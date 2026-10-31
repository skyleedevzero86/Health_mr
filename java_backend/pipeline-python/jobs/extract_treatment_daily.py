from __future__ import annotations

import argparse
import hashlib
import hmac
import json
import os
import sys
from collections import defaultdict
from datetime import date, timedelta
from pathlib import Path
from typing import Any

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))

from common.config import load_settings
from common.db_mysql import create_mysql_engine, fetch_rows
from common.logger import get_logger


EXTRACT_SQL = """
SELECT
    DATE(t.treatment_date) AS metric_date,
    COALESCE(d.department_name, t.treatment_dept, '미지정') AS department_name,
    CAST(COUNT(DISTINCT t.patient_no) AS UNSIGNED) AS patient_count,
    CAST(COUNT(*) AS UNSIGNED) AS treatment_count,
    CAST(COALESCE(SUM(COALESCE(p.total_medical_fee, 0)), 0) AS UNSIGNED) AS total_medical_fee
FROM treatments t
LEFT JOIN department d
    ON d.id = t.department_id
LEFT JOIN (
    SELECT
        treatment_id,
        SUM(COALESCE(payment_total_amount, 0)) AS total_medical_fee
    FROM `Payment`
    GROUP BY treatment_id
) p
    ON p.treatment_id = t.treatment_id
WHERE t.treatment_date IS NOT NULL
  AND DATE(t.treatment_date) = :target_date
GROUP BY DATE(t.treatment_date), COALESCE(d.department_name, t.treatment_dept, '미지정')
ORDER BY department_name
"""

PATIENT_KEY_SQL = """
SELECT
    DATE(t.treatment_date) AS metric_date,
    COALESCE(d.department_name, t.treatment_dept, '미지정') AS department_name,
    t.patient_no AS source_key
FROM treatments t
LEFT JOIN department d
    ON d.id = t.department_id
WHERE t.treatment_date IS NOT NULL
  AND DATE(t.treatment_date) = :target_date
GROUP BY DATE(t.treatment_date), COALESCE(d.department_name, t.treatment_dept, '미지정'), t.patient_no
"""


def default_target_date() -> str:
    return (date.today() - timedelta(days=1)).isoformat()


def load_patient_key_secret() -> str:
    secret = os.getenv("ANALYTICS_PATIENT_KEY_SECRET")
    if secret is None or secret.strip() == "":
        raise ValueError("Missing required environment variable: ANALYTICS_PATIENT_KEY_SECRET")
    return secret


def hash_patient_key(source_key: Any, secret: str) -> str:
    return hmac.new(
        secret.encode("utf-8"),
        str(source_key).encode("utf-8"),
        hashlib.sha256,
    ).hexdigest()


def build_export_rows(aggregate_rows: list[dict], patient_rows: list[dict], secret: str) -> list[dict]:
    hashes_by_grain: dict[tuple[Any, Any], set[str]] = defaultdict(set)
    for row in patient_rows:
        grain = (str(row["metric_date"])[:10], row["department_name"])
        hashes_by_grain[grain].add(hash_patient_key(row["source_key"], secret))

    export_rows = []
    for row in aggregate_rows:
        grain = (str(row["metric_date"])[:10], row["department_name"])
        export_rows.append(
            {
                "metric_date": str(row["metric_date"])[:10],
                "department_name": row["department_name"],
                "patient_count": int(row["patient_count"] or 0),
                "treatment_count": int(row["treatment_count"] or 0),
                "total_medical_fee": int(row["total_medical_fee"] or 0),
                "patient_key_hashes": sorted(hashes_by_grain.get(grain, set())),
            }
        )
    return export_rows


def save_payload(output_file: Path, rows: list[dict]) -> None:
    output_file.parent.mkdir(parents=True, exist_ok=True)
    output_file.write_text(json.dumps(rows, ensure_ascii=False, indent=2, default=str), encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="MySQL에서 일별 진료 분석 데이터를 추출합니다.")
    parser.add_argument("--target-date", default=default_target_date(), help="대상 날짜(YYYY-MM-DD)")
    args = parser.parse_args()

    settings = load_settings()
    secret = load_patient_key_secret()
    logger = get_logger("extract_treatment_daily", settings)
    engine = create_mysql_engine(settings)

    logger.info("진료 분석 데이터 추출 시작: %s", args.target_date)
    aggregate_rows = fetch_rows(engine, EXTRACT_SQL, {"target_date": args.target_date})
    patient_rows = fetch_rows(engine, PATIENT_KEY_SQL, {"target_date": args.target_date})
    rows = build_export_rows(aggregate_rows, patient_rows, secret)

    output_file = settings.data_dir / "exports" / f"treatment_daily_{args.target_date}.json"
    save_payload(output_file, rows)

    logger.info("추출 완료: 건수=%s, 출력파일=%s", len(rows), output_file)


if __name__ == "__main__":
    main()
