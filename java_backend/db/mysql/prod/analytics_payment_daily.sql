CREATE OR REPLACE VIEW analytics_payment_daily AS
SELECT
    DATE(COALESCE(p.payment_date, p.createdDate)) AS metric_date,
    COALESCE(p.payment_status, 'UNKNOWN') AS payment_status,
    COUNT(*) AS payment_count,
    COALESCE(SUM(COALESCE(p.payment_total_amount, 0)), 0) AS total_amount,
    COALESCE(
        SUM(
            CASE
                WHEN p.payment_status IN ('UNPAID', 'PARTIAL') THEN COALESCE(p.payment_remain_money, 0)
                ELSE 0
            END
        ),
        0
    ) AS unpaid_amount
FROM `Payment` p
WHERE COALESCE(p.payment_date, p.createdDate) IS NOT NULL
GROUP BY DATE(COALESCE(p.payment_date, p.createdDate)), COALESCE(p.payment_status, 'UNKNOWN');
