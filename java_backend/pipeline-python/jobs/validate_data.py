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

MYSQL_COUNT_SQL = """
SELECT
    COUNT(*) AS total_count,
    COUNT(DISTINCT patient_no) AS patient_count,
    COALESCE(SUM(p.total_amount), 0) AS total_fee
FROM treatment t
LEFT JOIN payment p ON p.treatment_id = t.treatment_id
WHERE DATE(t.created_date) = :target_date
"""

CLICKHOUSE_COUNT_SQL = """
SELECT
    sum(treatment_count) AS total_count,
    sum(patient_count) AS patient_count,
    sum(total_medical_fee) AS total_fee
FROM analytics_treatment_daily
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

    # MySQL 원본 집계
    engine = create_mysql_engine(settings)
    mysql_rows = fetch_rows(engine, MYSQL_COUNT_SQL, {"target_date": args.target_date})
    mysql_stats = mysql_rows[0] if mysql_rows else {"total_count": 0, "patient_count": 0, "total_fee": 0}

    # ClickHouse 분석 집계
    ch_client = create_clickhouse_client(settings)
    ch_result = ch_client.query(CLICKHOUSE_COUNT_SQL, parameters={"target_date": args.target_date})
    ch_row = ch_result.first_row if ch_result.row_count > 0 else (0, 0, 0)
    ch_stats = {"total_count": ch_row[0], "patient_count": ch_row[1], "total_fee": ch_row[2]}

    # 비교
    mismatches = []
    for key in ("total_count", "patient_count", "total_fee"):
        mysql_val = int(mysql_stats[key])
        ch_val = int(ch_stats[key])
        if mysql_val != ch_val:
            mismatches.append(f"  {key}: MySQL={mysql_val}, ClickHouse={ch_val}")

    if mismatches:
        logger.warning("정합성 불일치 발견 (%s):\n%s", args.target_date, "\n".join(mismatches))
        sys.exit(1)
    else:
        logger.info("정합성 검증 통과: %s (건수=%s, 환자=%s, 진료비=%s)",
                     args.target_date,
                     mysql_stats["total_count"],
                     mysql_stats["patient_count"],
                     mysql_stats["total_fee"])


if __name__ == "__main__":
    main()
