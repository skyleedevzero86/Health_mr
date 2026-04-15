# EMR 분석용 Python ETL 파이프라인

## 이 프로젝트의 역할

`pipeline-python`은 기존 Spring 기반 EMR 백엔드 옆에서 동작하는 **경량 ETL 사이드카**입니다.

핵심 역할은 다음과 같습니다.

- 운영 DB(MySQL)에서 분석용 데이터를 일 단위로 추출
- 분석 DB(ClickHouse)에 집계 데이터를 적재
- 실패 이력과 실행 이력을 남겨 재처리 가능한 배치 구조 제공

즉, 서비스 API는 Spring이 담당하고, 분석 적재 배치는 Python이 담당하는 **역할 분리 구조**입니다.

---

## 어떤 기능을 제공하나?

### 1) 일별 데이터 추출 (Extract)

- `jobs/extract_treatment_daily.py`
  - 진료 데이터를 일별/부서별 지표로 추출
- `jobs/extract_payment_daily.py`
  - 결제 데이터를 일별/상태별 지표로 추출
- 추출 결과는 JSON 파일로 `data/exports` 하위에 저장

### 2) ClickHouse 적재 (Load)

- `jobs/load_clickhouse.py`
  - 추출 JSON을 분석 테이블로 적재
  - 대상 데이터셋(`treatment_daily`, `payment_daily`) 선택 가능
  - 적재 성공/실패 건수 집계

### 3) 실행 이력 관리

- `etl_job_execution` 테이블에 실행 메타데이터 기록
  - 상태(SUCCESS/FAILED)
  - 원천 건수/적재 건수/오류 건수
  - 시작/종료 시각, 소요 시간

### 4) 오류 이력 관리

- `etl_job_error` 테이블에 오류 상세 기록
  - 오류 유형, 소스 키, 오류 메시지, 발생 시각

### 5) 실패 작업 재시도

- `jobs/retry_failed_jobs.py`
  - 실패한 적재 작업 조회
  - 재실행 모드(`--execute`)로 실제 재처리
  - 운영자가 장애 복구 루프를 간단히 돌릴 수 있음

---

## 왜 이 구조인가?

이 프로젝트는 의도적으로 범위를 작게 유지합니다.

- Airflow/Kafka/CDC 같은 대형 인프라 없이도
- `Python + 스케줄러(cron/작업 스케줄러) + ClickHouse`만으로
- 실무에서 많이 쓰는 분석 배치 패턴(추출-적재-이력-재시도)을 구현

목표는 “복잡한 플랫폼 구축”이 아니라, **운영 가능한 ETL 라이프사이클을 명확히 증명**하는 것입니다.

---

## 디렉터리 구조

```text
pipeline-python/
  .env.example
  requirements.txt
  README.md
  common/
    config.py          # 환경변수/설정 로드
    db_clickhouse.py   # ClickHouse 연결/적재 유틸
    db_mysql.py        # MySQL 조회 유틸
    logger.py          # 로거 설정
    metrics.py         # 실행 메트릭 모델
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

---

## 실행 흐름(일 배치 기준)

1. 진료/결제 데이터 추출
2. JSON 산출물 생성
3. ClickHouse 분석 테이블 적재
4. 실행/오류 이력 저장
5. 실패 건 재시도 배치 실행

권장 스케줄 예시:

- 02:00 진료 추출
- 02:10 진료 적재
- 02:20 결제 추출
- 02:30 결제 적재
- 03:00 실패 재시도

---

## 빠른 시작 (Windows)

```powershell
cd D:\intel\AISamples\Health_mr\java_backend\pipeline-python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
python .\jobs\extract_treatment_daily.py --target-date 2026-04-14
python .\jobs\load_clickhouse.py --dataset treatment_daily --target-date 2026-04-14
```

---

## 운영 시 참고 사항

- 추출 SQL은 템플릿이므로 실제 MySQL 물리 테이블/컬럼명에 맞춰 조정해야 합니다.
- JPA 엔티티명과 실제 테이블명이 다를 수 있어 스키마 확인이 필요합니다.
- 분석 지표는 “정의 일관성(예: 집계 기준 시각/상태값)”이 중요합니다.
- 장애 대응을 위해 `etl_job_execution`, `etl_job_error` 모니터링을 권장합니다.
