CREATE OR REPLACE VIEW feature_usage_history AS
SELECT
    id,
    user_id,
    action_type,
    entity_type,
    entity_id,
    CASE
        WHEN JSON_VALID(before_data) THEN JSON_UNQUOTE(JSON_EXTRACT(before_data, '$.sourceDatabase'))
        ELSE NULL
    END AS source_database,
    CASE
        WHEN JSON_VALID(before_data) THEN JSON_UNQUOTE(JSON_EXTRACT(before_data, '$.sourceTable'))
        ELSE NULL
    END AS source_table,
    CASE
        WHEN JSON_VALID(before_data) THEN JSON_UNQUOTE(JSON_EXTRACT(before_data, '$.startDate'))
        ELSE NULL
    END AS start_date,
    CASE
        WHEN JSON_VALID(before_data) THEN JSON_UNQUOTE(JSON_EXTRACT(before_data, '$.endDate'))
        ELSE NULL
    END AS end_date,
    CASE
        WHEN JSON_VALID(after_data) THEN JSON_UNQUOTE(JSON_EXTRACT(after_data, '$.rowCount'))
        ELSE NULL
    END AS row_count,
    CASE
        WHEN JSON_VALID(after_data) THEN JSON_UNQUOTE(JSON_EXTRACT(after_data, '$.fileName'))
        ELSE NULL
    END AS file_name,
    before_data,
    after_data,
    created_at
FROM audit_log
WHERE action_type IN ('AI_USAGE', 'CLICKHOUSE_USAGE', 'MYSQL_ANALYTICS_USAGE');
