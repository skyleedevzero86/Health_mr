# Python ETL Sidecar for EMR Analytics

## Purpose

This directory adds a small Python sidecar to the current Spring-based EMR backend.

The goal is not to rebuild the system around Python. The goal is to show:

- Python-based ETL
- cron-friendly batch execution
- MySQL to ClickHouse analytics flow
- retry and job history concepts
- a portfolio story that matches the target job posting

## Why This Is Not Overengineering

This sidecar is intentionally small.

- It does not add Airflow.
- It does not add Kafka.
- It does not add streaming or CDC.
- It does not replace Spring batch or Spring business APIs.
- It only covers daily analytics extraction and loading.

The scope stays within a portfolio-friendly boundary:

- 2 daily extraction jobs
- 2 analytics tables
- 1 retry flow
- 1 job execution history model

That is enough to prove `Python + cron + analytics pipeline` without turning the project into an infrastructure showcase.

## Recommended Role Split

- Spring Boot
  - business APIs
  - authentication and authorization
  - masking and audit log
  - admin APIs for job history and dashboards
- Python sidecar
  - extract daily aggregates from MySQL
  - transform into analytics-ready payloads
  - load into ClickHouse
  - record failures for retry
- ClickHouse
  - large read-heavy analytics queries

## Directory Layout

```text
pipeline-python/
  .env.example
  requirements.txt
  README.md
  common/
    config.py
    db_clickhouse.py
    db_mysql.py
    logger.py
    metrics.py
  jobs/
    extract_payment_daily.py
    extract_treatment_daily.py
    load_clickhouse.py
    retry_failed_jobs.py
  scripts/
    run_daily_pipeline.ps1
    run_retry_pipeline.ps1
  sql/
    clickhouse/
      analytics_payment_daily.sql
      analytics_treatment_daily.sql
      etl_job_error.sql
      etl_job_execution.sql
```

## Quick Start

1. Create a local virtual environment.
2. Install dependencies.
3. Copy `.env.example` to `.env`.
4. Fill in MySQL and ClickHouse connection values.
5. Create the ClickHouse tables under `sql/clickhouse`.
6. Run the extraction and loading scripts.

Windows example:

```powershell
cd D:\intel\AISamples\Health_mr\java_backend\pipeline-python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
python .\jobs\extract_treatment_daily.py --target-date 2026-04-14
python .\jobs\load_clickhouse.py --dataset treatment_daily --target-date 2026-04-14
```

## Important Notes

- The SQL in the job scripts is a template. Adjust table names to match your real MySQL schema.
- This repo is JPA-centered, so physical table names can differ from entity names.
- For portfolio usage, the important part is the architecture and job lifecycle, not whether the very first draft query is perfect.

## Suggested Cron Schedule

For production explanation:

- `02:00` extract treatment analytics
- `02:10` load treatment analytics
- `02:20` extract payment analytics
- `02:30` load payment analytics
- `03:00` retry failed jobs

Linux cron example:

```cron
0 2 * * * /usr/bin/python /opt/emr/pipeline-python/jobs/extract_treatment_daily.py
10 2 * * * /usr/bin/python /opt/emr/pipeline-python/jobs/load_clickhouse.py --dataset treatment_daily
20 2 * * * /usr/bin/python /opt/emr/pipeline-python/jobs/extract_payment_daily.py
30 2 * * * /usr/bin/python /opt/emr/pipeline-python/jobs/load_clickhouse.py --dataset payment_daily
0 3 * * * /usr/bin/python /opt/emr/pipeline-python/jobs/retry_failed_jobs.py --execute
```

For your local Windows environment, use the PowerShell scripts under `scripts/`.
