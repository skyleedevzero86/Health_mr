-- ETL 실행 메타/성과 이력 테이블
-- 목적:
-- 1) 실행 성공/실패, 처리량, 소요시간을 표준 포맷으로 기록한다.
-- 2) SLA 모니터링 및 실패 재시도 전략의 근거 데이터를 제공한다.
CREATE TABLE IF NOT EXISTS etl_job_execution
(
    -- 실행 단위 고유 ID
    job_execution_id UUID,
    -- 작업명
    job_name String,
    -- 대상 비즈니스 날짜
    target_date Date,
    -- 실행 상태
    status LowCardinality(String),
    -- 원천 조회 건수
    source_count UInt64,
    -- 최종 적재 건수
    loaded_count UInt64,
    -- 오류 건수
    error_count UInt64,
    -- 실행 시작 시각
    started_at DateTime,
    -- 실행 종료 시각
    finished_at DateTime,
    -- 실행 시간 성능 회귀 모니터링 핵심
    elapsed_ms UInt64,
    -- 입력 파일/소스 경로
    input_file String,
    -- 실패 시 대표 오류 메시지
    error_message String
)
ENGINE = MergeTree
-- 실행 시각 기준 월 파티션
PARTITION BY toYYYYMM(started_at)
-- 작업별 시간순 운영 조회 패턴 최적화
ORDER BY (job_name, started_at, job_execution_id);
