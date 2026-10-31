CREATE TABLE IF NOT EXISTS analytics_treatment_daily
(
    metric_date Date,
    department_name String,
    patient_count UInt64,
    treatment_count UInt64,
    total_medical_fee UInt64,
    unique_patient_state AggregateFunction(uniqExact, String)
)
ENGINE = MergeTree
PARTITION BY metric_date
ORDER BY (metric_date, department_name);
