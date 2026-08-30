"""월간 진료 통계 리포트 자동 생성 스크립트.

ClickHouse에서 월간 집계 데이터를 조회하여 Markdown 리포트를 생성합니다.
생성된 리포트는 data/reports/ 디렉토리에 저장됩니다.
"""
from __future__ import annotations

import argparse
import sys
from datetime import date
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))

from common.config import load_settings
from common.db_clickhouse import create_clickhouse_client
from common.logger import get_logger

MONTHLY_SUMMARY_SQL = """
SELECT
    metric_date,
    department_name,
    sum(patient_count) AS patient_count,
    sum(treatment_count) AS treatment_count,
    sum(total_medical_fee) AS total_medical_fee
FROM analytics_treatment_daily
WHERE metric_date >= %(start_date)s AND metric_date < %(end_date)s
GROUP BY metric_date, department_name
ORDER BY metric_date, department_name
"""

MONTHLY_TOTALS_SQL = """
SELECT
    sum(patient_count) AS total_patients,
    sum(treatment_count) AS total_treatments,
    sum(total_medical_fee) AS total_fee,
    count(DISTINCT metric_date) AS working_days
FROM analytics_treatment_daily
WHERE metric_date >= %(start_date)s AND metric_date < %(end_date)s
"""

DEPARTMENT_RANKING_SQL = """
SELECT
    department_name,
    sum(patient_count) AS patient_count,
    sum(treatment_count) AS treatment_count,
    sum(total_medical_fee) AS total_fee
FROM analytics_treatment_daily
WHERE metric_date >= %(start_date)s AND metric_date < %(end_date)s
GROUP BY department_name
ORDER BY treatment_count DESC
"""


def resolve_month(year_month: str | None) -> tuple[str, str, str]:
    """year_month(YYYY-MM) -> (start_date, end_date, display_label)"""
    if year_month:
        year, month = map(int, year_month.split("-"))
    else:
        today = date.today().replace(day=1)
        prev = (today - __import__("datetime").timedelta(days=1))
        year, month = prev.year, prev.month

    start = date(year, month, 1)
    if month == 12:
        end = date(year + 1, 1, 1)
    else:
        end = date(year, month + 1, 1)

    return start.isoformat(), end.isoformat(), f"{year}년 {month:02d}월"


def build_report(label: str, totals: dict, departments: list[dict], daily: list[dict]) -> str:
    lines = [
        f"# {label} 진료 통계 월간 리포트",
        "",
        "## 1. 월간 요약",
        "",
        f"| 지표 | 값 |",
        f"|------|-----|",
        f"| 총 진료건수 | {totals.get('total_treatments', 0):,} |",
        f"| 총 환자수 | {totals.get('total_patients', 0):,} |",
        f"| 총 진료비 | {totals.get('total_fee', 0):,.0f}원 |",
        f"| 영업일수 | {totals.get('working_days', 0)} |",
        "",
        "## 2. 진료과별 순위",
        "",
        "| 순위 | 진료과 | 진료건수 | 환자수 | 진료비 |",
        "|------|--------|----------|--------|--------|",
    ]

    for i, dept in enumerate(departments, 1):
        lines.append(
            f"| {i} | {dept['department_name']} | {dept['treatment_count']:,} "
            f"| {dept['patient_count']:,} | {dept['total_fee']:,.0f}원 |"
        )

    lines.extend([
        "",
        "## 3. 일별 추이",
        "",
        "| 날짜 | 진료과 | 환자수 | 진료건수 | 진료비 |",
        "|------|--------|--------|----------|--------|",
    ])

    for row in daily:
        lines.append(
            f"| {row['metric_date']} | {row['department_name']} "
            f"| {row['patient_count']:,} | {row['treatment_count']:,} "
            f"| {row['total_medical_fee']:,.0f}원 |"
        )

    lines.extend(["", f"---", f"자동 생성: pipeline-python/jobs/generate_monthly_report.py"])
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="월간 진료 통계 리포트 생성")
    parser.add_argument("--month", default=None, help="대상 월(YYYY-MM). 미지정 시 전월")
    args = parser.parse_args()

    settings = load_settings()
    logger = get_logger("generate_monthly_report", settings)

    start_date, end_date, label = resolve_month(args.month)
    logger.info("월간 리포트 생성 시작: %s (%s ~ %s)", label, start_date, end_date)

    ch_client = create_clickhouse_client(settings)
    params = {"start_date": start_date, "end_date": end_date}

    # 월간 합계
    totals_result = ch_client.query(MONTHLY_TOTALS_SQL, parameters=params)
    row = totals_result.first_row if totals_result.row_count > 0 else (0, 0, 0, 0)
    totals = {
        "total_patients": row[0],
        "total_treatments": row[1],
        "total_fee": row[2],
        "working_days": row[3],
    }

    # 진료과별 순위
    dept_result = ch_client.query(DEPARTMENT_RANKING_SQL, parameters=params)
    departments = [
        {"department_name": r[0], "patient_count": r[1], "treatment_count": r[2], "total_fee": r[3]}
        for r in dept_result.result_rows
    ]

    # 일별 상세
    daily_result = ch_client.query(MONTHLY_SUMMARY_SQL, parameters=params)
    daily = [
        {
            "metric_date": r[0],
            "department_name": r[1],
            "patient_count": r[2],
            "treatment_count": r[3],
            "total_medical_fee": r[4],
        }
        for r in daily_result.result_rows
    ]

    report = build_report(label, totals, departments, daily)

    report_dir = settings.data_dir / "reports"
    report_dir.mkdir(parents=True, exist_ok=True)
    report_file = report_dir / f"monthly_report_{start_date[:7]}.md"
    report_file.write_text(report, encoding="utf-8")

    logger.info("월간 리포트 생성 완료: %s (진료건수=%s, 환자수=%s)",
                report_file, totals["total_treatments"], totals["total_patients"])


if __name__ == "__main__":
    main()
