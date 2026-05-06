-- ETL 오류 상세 이력 테이블
-- 목적:
-- 1) 실행 단위 실패를 추적하고 재처리 대상을 식별한다.
-- 2) 장애 원인을 보존해 사후 분석 및 재현성을 높인다.
CREATE TABLE IF NOT EXISTS etl_job_error
(
    -- 오류 이벤트 고유 ID
    job_error_id UUID,
    -- 어떤 실행에서 발생했는지 연결하는 FK 역할 키
    job_execution_id UUID,
    -- 작업명
    job_name String,
    -- 원본 데이터 식별자
    source_key String,
    -- 오류 분류 코드
    error_type LowCardinality(String),
    -- 오류가 난 원본 payload 또는 참조값
    payload String,
    -- 사람이 읽는 오류 메시지
    error_message String,
    -- 오류 발생 시각
    created_at DateTime
)
ENGINE = MergeTree
-- 월 파티션으로 장애 추세 조회 및 보관 정책 적용 용이
PARTITION BY toYYYYMM(created_at)
-- 작업명 + 시간순 조회, 동일 시각 다건 발생 시 UUID로 안정 정렬
ORDER BY (job_name, created_at, job_error_id);
