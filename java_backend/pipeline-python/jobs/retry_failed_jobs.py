from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))

from common.config import load_settings
from common.db_clickhouse import create_clickhouse_client
from common.logger import get_logger


FAILED_JOBS_QUERY = """
SELECT
    job_execution_id,
    job_name,
    target_date,
    input_file
FROM etl_job_execution
WHERE status = 'FAILED'
ORDER BY finished_at DESC
LIMIT {limit}
"""


def dataset_from_job_name(job_name: str) -> str | None:
    if job_name == "load_treatment_daily":
        return "treatment_daily"
    if job_name == "load_payment_daily":
        return "payment_daily"
    return None


def run_retry(dataset: str, target_date: str, input_file: str) -> int:
    job_script = Path(__file__).resolve().parent / "load_clickhouse.py"
    command = [
        sys.executable,
        str(job_script),
        "--dataset",
        dataset,
        "--target-date",
        target_date,
        "--input-file",
        input_file,
    ]
    return subprocess.run(command, check=False).returncode


def main() -> None:
    parser = argparse.ArgumentParser(description="실패한 ClickHouse 적재 작업을 재시도합니다.")
    parser.add_argument("--limit", type=int, default=10)
    parser.add_argument("--execute", action="store_true", help="실제로 실패 작업을 재실행합니다.")
    args = parser.parse_args()

    settings = load_settings()
    logger = get_logger("retry_failed_jobs", settings)
    client = create_clickhouse_client(settings)

    query = FAILED_JOBS_QUERY.format(limit=args.limit)
    result = client.query(query)
    rows = result.named_results()

    if not rows:
        logger.info("재시도할 실패 작업이 없습니다.")
        return

    for row in rows:
        dataset = dataset_from_job_name(row["job_name"])
        if dataset is None:
            logger.warning("알 수 없는 작업명이라 건너뜁니다: 작업명=%s", row["job_name"])
            continue

        logger.info(
            "실패 작업 발견: 실행ID=%s, 데이터셋=%s, 대상일=%s, 입력파일=%s",
            row["job_execution_id"],
            dataset,
            row["target_date"],
            row["input_file"],
        )

        if not args.execute:
            continue

        exit_code = run_retry(dataset, row["target_date"], row["input_file"])
        if exit_code == 0:
            logger.info("재시도 성공: 실행ID=%s", row["job_execution_id"])
        else:
            logger.error("재시도 재실패: 실행ID=%s", row["job_execution_id"])


if __name__ == "__main__":
    main()
