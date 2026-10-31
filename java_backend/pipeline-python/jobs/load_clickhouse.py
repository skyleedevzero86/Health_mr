from __future__ import annotations

import argparse
import json
import sys
from datetime import date, datetime, timedelta, timezone
from pathlib import Path
from uuid import uuid4

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))

from common.config import load_settings
from common.db_clickhouse import create_clickhouse_client, insert_rows
from common.logger import get_logger
from common.metrics import JobMetrics


DATASET_TABLE_MAP = {
    "treatment_daily": "analytics_treatment_daily",
    "payment_daily": "analytics_payment_daily",
}


def default_target_date() -> str:
    return (date.today() - timedelta(days=1)).isoformat()


def default_input_file(settings, dataset: str, target_date: str) -> Path:
    return settings.data_dir / "exports" / f"{dataset}_{target_date}.json"


def load_json_rows(input_file: Path) -> list[dict]:
    if not input_file.exists():
        raise FileNotFoundError(f"입력 파일을 찾을 수 없습니다: {input_file}")
    rows = json.loads(input_file.read_text(encoding="utf-8"))
    int_keys = (
        "patient_count",
        "treatment_count",
        "total_medical_fee",
        "payment_count",
        "total_amount",
        "unpaid_amount",
    )
    coerced = []
    for row in rows:
        item = dict(row)
        metric_date = item.get("metric_date")
        if isinstance(metric_date, str):
            item["metric_date"] = date.fromisoformat(metric_date[:10])
        for key in int_keys:
            if key in item and item[key] is not None:
                item[key] = int(item[key])
        hashes = item.get("patient_key_hashes")
        if hashes is not None:
            item["patient_key_hashes"] = [str(value) for value in hashes]
        coerced.append(item)
    return coerced


def record_job_execution(client, job_execution_id: str, metrics: JobMetrics, dataset: str, input_file: str) -> None:
    row = {
        "job_execution_id": job_execution_id,
        "job_name": f"load_{dataset}",
        "target_date": date.fromisoformat(str(metrics.target_date)[:10]),
        "status": metrics.status,
        "source_count": metrics.source_count,
        "loaded_count": metrics.loaded_count,
        "error_count": metrics.error_count,
        "started_at": metrics.started_at.replace(tzinfo=None),
        "finished_at": (metrics.finished_at or datetime.now(timezone.utc)).replace(tzinfo=None),
        "elapsed_ms": metrics.elapsed_ms() or 0,
        "input_file": input_file,
        "error_message": metrics.error_message or "",
    }
    insert_rows(client, "etl_job_execution", [row])


def drop_partition_if_exists(client, table_name: str, partition: str) -> None:
    try:
        client.command(f"ALTER TABLE {table_name} DROP PARTITION '{partition}'")
    except Exception as exc:
        message = str(exc)
        if "No partition" not in message and "Unknown partition" not in message:
            raise


def insert_treatment_unique_rows(client, tmp_name: str, day_rows: list[dict]) -> None:
    raw_name = f"{tmp_name}_raw"
    try:
        client.command(
            f"""
            CREATE TABLE {raw_name}
            (
                metric_date Date,
                department_name String,
                patient_count UInt64,
                treatment_count UInt64,
                total_medical_fee UInt64,
                patient_key_hash String
            )
            ENGINE = Memory
            """
        )
        raw_rows = []
        for row in day_rows:
            hashes = row.get("patient_key_hashes") or [""]
            if not hashes:
                hashes = [""]
            for patient_key_hash in hashes:
                raw_rows.append(
                    {
                        "metric_date": row["metric_date"],
                        "department_name": row["department_name"],
                        "patient_count": int(row.get("patient_count") or 0),
                        "treatment_count": int(row.get("treatment_count") or 0),
                        "total_medical_fee": int(row.get("total_medical_fee") or 0),
                        "patient_key_hash": patient_key_hash,
                    }
                )
        insert_rows(client, raw_name, raw_rows)
        client.command(
            f"""
            INSERT INTO {tmp_name}
            SELECT
                metric_date,
                department_name,
                any(patient_count),
                any(treatment_count),
                any(total_medical_fee),
                uniqExactState(nullIf(patient_key_hash, ''))
            FROM {raw_name}
            GROUP BY metric_date, department_name
            """
        )
    finally:
        client.command(f"DROP TABLE IF EXISTS {raw_name}")


def replace_day_partition(client, table_name: str, target_date: str, rows: list[dict]) -> int:
    target = date.fromisoformat(str(target_date)[:10])
    partition = target.isoformat()
    day_rows = [row for row in rows if row.get("metric_date") == target]
    tmp_name = f"{table_name}_tmp_{uuid4().hex}"
    try:
        client.command(f"CREATE TABLE {tmp_name} AS {table_name}")
        if day_rows:
            if table_name == "analytics_treatment_daily":
                insert_treatment_unique_rows(client, tmp_name, day_rows)
            else:
                insert_rows(client, tmp_name, day_rows)
            client.command(
                f"ALTER TABLE {table_name} REPLACE PARTITION '{partition}' FROM {tmp_name}"
            )
        else:
            drop_partition_if_exists(client, table_name, partition)
        return len(day_rows)
    finally:
        client.command(f"DROP TABLE IF EXISTS {tmp_name}")


def record_job_error(client, job_execution_id: str, dataset: str, input_file: str, error_message: str) -> None:
    row = {
        "job_error_id": str(uuid4()),
        "job_execution_id": job_execution_id,
        "job_name": f"load_{dataset}",
        "source_key": input_file,
        "error_type": "LOAD_ERROR",
        "payload": input_file,
        "error_message": error_message,
        "created_at": datetime.now(timezone.utc).replace(tzinfo=None),
    }
    insert_rows(client, "etl_job_error", [row])


def main() -> None:
    parser = argparse.ArgumentParser(description="추출된 분석 데이터를 ClickHouse에 적재합니다.")
    parser.add_argument("--dataset", choices=sorted(DATASET_TABLE_MAP.keys()), required=True)
    parser.add_argument("--target-date", default=default_target_date(), help="대상 날짜(YYYY-MM-DD)")
    parser.add_argument("--input-file", help="선택 입력 파일 경로")
    args = parser.parse_args()

    settings = load_settings()
    logger = get_logger("load_clickhouse", settings)
    client = create_clickhouse_client(settings)

    input_file = Path(args.input_file) if args.input_file else default_input_file(settings, args.dataset, args.target_date)
    table_name = DATASET_TABLE_MAP[args.dataset]
    job_execution_id = str(uuid4())
    metrics = JobMetrics(job_name=f"load_{args.dataset}", target_date=args.target_date)

    logger.info("데이터 적재 시작: 데이터셋=%s, 테이블=%s, 입력파일=%s", args.dataset, table_name, input_file)

    try:
        rows = load_json_rows(input_file)
        metrics.source_count = len(rows)
        loaded_count = replace_day_partition(client, table_name, args.target_date, rows)
        metrics.mark_success(loaded_count)
        record_job_execution(client, job_execution_id, metrics, args.dataset, str(input_file))
        logger.info("데이터 적재 완료: 데이터셋=%s, 적재건수=%s", args.dataset, loaded_count)
    except Exception as exc:
        metrics.mark_failed(str(exc))
        record_job_execution(client, job_execution_id, metrics, args.dataset, str(input_file))
        record_job_error(client, job_execution_id, args.dataset, str(input_file), str(exc))
        logger.exception("데이터 적재 실패: 데이터셋=%s", args.dataset)
        raise


if __name__ == "__main__":
    main()
