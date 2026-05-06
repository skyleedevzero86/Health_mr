CREATE OR REPLACE VIEW analytics_treatment_daily AS
SELECT
    DATE(t.treatment_date) AS metric_date,
    COALESCE(d.department_name, t.treatment_dept, '미지정') AS department_name,
    COUNT(DISTINCT t.patient_no) AS patient_count,
    COUNT(*) AS treatment_count,
    COALESCE(SUM(COALESCE(p.total_medical_fee, 0)), 0) AS total_medical_fee
FROM treatments t
LEFT JOIN department d
    ON d.id = t.department_id
LEFT JOIN (
    SELECT
        treatment_id,
        SUM(COALESCE(payment_total_amount, 0)) AS total_medical_fee
    FROM payment
    GROUP BY treatment_id
) p
    ON p.treatment_id = t.treatment_id
WHERE t.treatment_date IS NOT NULL
GROUP BY DATE(t.treatment_date), COALESCE(d.department_name, t.treatment_dept, '미지정');
