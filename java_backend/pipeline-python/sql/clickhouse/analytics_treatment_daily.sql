-- 진료 일별 집계 팩트 테이블
-- 목적:
-- 1) 부서 단위 진료량/환자수/진료비를 일 단위로 표준 집계한다.
-- 2) 진료 운영 지표와 재무 지표를 같은 시간 축에서 결합 분석할 기반을 제공한다.
CREATE TABLE IF NOT EXISTS analytics_treatment_daily
(
    -- 집계 기준 일자
    metric_date Date,
    -- 진료 부서명
    department_name String,
    -- 중복 제거 환자 수
    patient_count UInt64,
    -- 총 진료 건수
    treatment_count UInt64,
    -- 진료비 총액
    total_medical_fee UInt64
)
ENGINE = MergeTree
-- 월 단위 보관/백필 운영에 최적화
PARTITION BY toYYYYMM(metric_date)
-- 기간 + 부서 기준 탐색이 잦으므로 해당 순서로 정렬
ORDER BY (metric_date, department_name);
