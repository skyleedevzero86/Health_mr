"""데이터 정합성 검증 스크립트.

MySQL 원본 데이터와 ClickHouse 분석 데이터 간 건수/합계 일치 여부를 검증합니다.
불일치가 발견되면 경고를 로그에 기록하고 종료 코드 1을 반환합니다.
"""
from __future__ import annotations

import argparse
import sys
from datetime import date, timedelta
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))

from common.config import load_settings
from common.db_clickhouse import create_clickhouse_client
from common.db_mysql import create_mysql_engine, fetch_rows
from common.logger import get_logger

MYSQL_TREATMENT_SQL = """
SELECT
    CAST(COALESCE(SUM(v.treatment_count), 0) AS UNSIGNED) AS total_count,
    CAST(COALESCE(SUM(v.patient_count), 0) AS UNSIGNED) AS visit_patient_count,
    CAST(COALESCE((
        SELECT COUNT(DISTINCT t.patient_no)
        FROM treatments t
        WHERE t.treatment_date IS NOT NULL
          AND DATE(t.treatment_date) = :target_date
    ), 0) AS UNSIGNED) AS unique_patient_count,
    CAST(COALESCE(SUM(v.total_medical_fee), 0) AS UNSIGNED) AS total_fee
FROM analytics_treatment_daily v
WHERE v.metric_date = :target_date
"""

MYSQL_PAYMENT_SQL = """
SELECT
    CAST(COALESCE(SUM(payment_count), 0) AS UNSIGNED) AS payment_count,
    CAST(COALESCE(SUM(total_amount), 0) AS UNSIGNED) AS total_amount,
    CAST(COALESCE(SUM(unpaid_amount), 0) AS UNSIGNED) AS unpaid_amount
FROM analytics_payment_daily
WHERE metric_date = :target_date
"""

CLICKHOUSE_TREATMENT_SQL = """
SELECT
    sum(treatment_count) AS total_count,
    sum(patient_count) AS visit_patient_count,
    uniqExactMerge(unique_patient_state) AS unique_patient_count,
    sum(total_medical_fee) AS total_fee
FROM analytics_treatment_daily
WHERE metric_date = %(target_date)s
"""

CLICKHOUSE_PAYMENT_SQL = """
SELECT
    sum(payment_count) AS payment_count,
    sum(total_amount) AS total_amount,
    sum(unpaid_amount) AS unpaid_amount
FROM analytics_payment_daily
WHERE metric_date = %(target_date)s
"""


def default_target_date() -> str:
    return (date.today() - timedelta(days=1)).isoformat()


def main() -> None:
    parser = argparse.ArgumentParser(description="MySQL-ClickHouse 데이터 정합성 검증")
    parser.add_argument("--target-date", default=default_target_date(), help="검증 대상 날짜(YYYY-MM-DD)")
    args = parser.parse_args()

    settings = load_settings()
    logger = get_logger("validate_data", settings)

    logger.info("데이터 정합성 검증 시작: %s", args.target_date)

    engine = create_mysql_engine(settings)
    mysql_treatment = fetch_rows(engine, MYSQL_TREATMENT_SQL, {"target_date": args.target_date})
    mysql_payment = fetch_rows(engine, MYSQL_PAYMENT_SQL, {"target_date": args.target_date})
    mysql_t = mysql_treatment[0] if mysql_treatment else {
        "total_count": 0,
        "visit_patient_count": 0,
        "unique_patient_count": 0,
        "total_fee": 0,
    }
    mysql_p = mysql_payment[0] if mysql_payment else {"payment_count": 0, "total_amount": 0, "unpaid_amount": 0}

    ch_client = create_clickhouse_client(settings)
    ch_t_result = ch_client.query(CLICKHOUSE_TREATMENT_SQL, parameters={"target_date": args.target_date})
    ch_p_result = ch_client.query(CLICKHOUSE_PAYMENT_SQL, parameters={"target_date": args.target_date})
    ch_t_row = ch_t_result.first_row if ch_t_result.row_count > 0 else (0, 0, 0, 0)
    ch_p_row = ch_p_result.first_row if ch_p_result.row_count > 0 else (0, 0, 0)
    ch_t = {
        "total_count": ch_t_row[0] or 0,
        "visit_patient_count": ch_t_row[1] or 0,
        "unique_patient_count": ch_t_row[2] or 0,
        "total_fee": ch_t_row[3] or 0,
    }
    ch_p = {
        "payment_count": ch_p_row[0] or 0,
        "total_amount": ch_p_row[1] or 0,
        "unpaid_amount": ch_p_row[2] or 0,
    }

    mismatches = []
    for key in ("total_count", "visit_patient_count", "unique_patient_count", "total_fee"):
        mysql_val = int(mysql_t[key] or 0)
        ch_val = int(ch_t[key] or 0)
        if mysql_val != ch_val:
            mismatches.append(f"  treatment.{key}: MySQL={mysql_val}, ClickHouse={ch_val}")
    for key in ("payment_count", "total_amount", "unpaid_amount"):
        mysql_val = int(mysql_p[key] or 0)
        ch_val = int(ch_p[key] or 0)
        if mysql_val != ch_val:
            mismatches.append(f"  payment.{key}: MySQL={mysql_val}, ClickHouse={ch_val}")

    if mismatches:
        logger.warning("정합성 불일치 발견 (%s):\n%s", args.target_date, "\n".join(mismatches))
        sys.exit(1)

    logger.info(
        "정합성 검증 통과: %s (진료=%s, 연환자=%s, 실환자=%s, 진료비=%s, 결제=%s, 결제액=%s, 미수=%s)",
        args.target_date,
        mysql_t["total_count"],
        mysql_t["visit_patient_count"],
        mysql_t["unique_patient_count"],
        mysql_t["total_fee"],
        mysql_p["payment_count"],
        mysql_p["total_amount"],
        mysql_p["unpaid_amount"],
    )


if __name__ == "__main__":
    main()
