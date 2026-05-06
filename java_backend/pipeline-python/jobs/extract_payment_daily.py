from __future__ import annotations

import argparse
import json
import sys
from datetime import date, timedelta
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))

from common.config import load_settings
from common.db_mysql import create_mysql_engine, fetch_rows
from common.logger import get_logger


# Update table and column names according to the real MySQL schema in this repo.
EXTRACT_SQL = """
SELECT
    DATE(p.created_date) AS metric_date,
    p.status AS payment_status,
    COUNT(*) AS payment_count,
    COALESCE(SUM(p.total_amount), 0) AS total_amount,
    COALESCE(SUM(CASE WHEN p.status = 'UNPAID' THEN p.total_amount ELSE 0 END), 0) AS unpaid_amount
FROM payment p
WHERE DATE(p.created_date) = :target_date
GROUP BY DATE(p.created_date), p.status
ORDER BY payment_status
"""


def default_target_date() -> str:
    return (date.today() - timedelta(days=1)).isoformat()


def save_payload(output_file: Path, rows: list[dict]) -> None:
    output_file.parent.mkdir(parents=True, exist_ok=True)
    output_file.write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="MySQL에서 일별 결제 분석 데이터를 추출합니다.")
    parser.add_argument("--target-date", default=default_target_date(), help="대상 날짜(YYYY-MM-DD)")
    args = parser.parse_args()

    settings = load_settings()
    logger = get_logger("extract_payment_daily", settings)
    engine = create_mysql_engine(settings)

    logger.info("결제 분석 데이터 추출 시작: %s", args.target_date)
    rows = fetch_rows(engine, EXTRACT_SQL, {"target_date": args.target_date})

    output_file = settings.data_dir / "exports" / f"payment_daily_{args.target_date}.json"
    save_payload(output_file, rows)

    logger.info("추출 완료: 건수=%s, 출력파일=%s", len(rows), output_file)


if __name__ == "__main__":
    main()
