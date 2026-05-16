DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'airflow') THEN
        CREATE DATABASE airflow;
    END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE airflow TO emr_pg_user;
