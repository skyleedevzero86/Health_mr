-- 결제 일별 집계 팩트 테이블
-- 목적:
-- 1) OLTP 원천 데이터를 일 단위로 비정규화/집계하여 조회 성능을 확보한다.
-- 2) 대시보드/리포트에서 자주 사용하는 날짜+상태 기준 필터를 빠르게 처리한다.
CREATE TABLE IF NOT EXISTS analytics_payment_daily
(
    -- 집계 기준 일자(UTC/로컬 기준은 ETL에서 일관되게 맞춰야 함)
    metric_date Date,
    -- 결제 상태 상태값 표준화는 ETL 단계에서 보장 권장
    payment_status String,
    -- 해당 일자+상태 조합의 결제 건수
    payment_count UInt64,
    -- 해당 그룹의 총 결제 예정 금액 합계
    total_amount UInt64,
    -- 미수 상태 금액 합계
    unpaid_amount UInt64
)
ENGINE = MergeTree
-- 월 단위 파티션: 운영/백필 시 범위 삭제 및 재적재 단위를 월로 통일
PARTITION BY toYYYYMM(metric_date)
-- 대표 조회 패턴에 맞춘 정렬 키
ORDER BY (metric_date, payment_status);
