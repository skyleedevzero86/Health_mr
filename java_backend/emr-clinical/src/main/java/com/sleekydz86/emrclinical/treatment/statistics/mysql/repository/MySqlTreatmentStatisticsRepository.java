package com.sleekydz86.emrclinical.treatment.statistics.mysql.repository;

import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@Profile("!clickhouse")
public class MySqlTreatmentStatisticsRepository {

    private static final String TREATMENT_SUMMARY_SQL = """
            SELECT
                COALESCE(SUM(v.patient_count), 0) AS visit_patient_count,
                COALESCE((
                    SELECT COUNT(DISTINCT t.patient_no)
                    FROM treatments t
                    WHERE t.treatment_date IS NOT NULL
                      AND DATE(t.treatment_date) BETWEEN :startDate AND :endDate
                ), 0) AS unique_patient_count,
                COALESCE(SUM(v.treatment_count), 0) AS treatment_count,
                COALESCE(SUM(v.total_medical_fee), 0) AS total_medical_fee
            FROM analytics_treatment_daily v
            WHERE v.metric_date BETWEEN :startDate AND :endDate
            """;

    private static final String TREATMENT_DAILY_SQL = """
            SELECT
                v.metric_date,
                SUM(v.patient_count) AS visit_patient_count,
                COALESCE(u.unique_patient_count, 0) AS unique_patient_count,
                SUM(v.treatment_count) AS treatment_count,
                SUM(v.total_medical_fee) AS total_medical_fee
            FROM analytics_treatment_daily v
            LEFT JOIN (
                SELECT
                    DATE(t.treatment_date) AS metric_date,
                    COUNT(DISTINCT t.patient_no) AS unique_patient_count
                FROM treatments t
                WHERE t.treatment_date IS NOT NULL
                  AND DATE(t.treatment_date) BETWEEN :startDate AND :endDate
                GROUP BY DATE(t.treatment_date)
            ) u
                ON u.metric_date = v.metric_date
            WHERE v.metric_date BETWEEN :startDate AND :endDate
            GROUP BY v.metric_date, u.unique_patient_count
            ORDER BY v.metric_date
            """;

    private static final String TREATMENT_DEPARTMENT_SQL = """
            SELECT
                v.department_name,
                SUM(v.patient_count) AS visit_patient_count,
                COALESCE(u.unique_patient_count, 0) AS unique_patient_count,
                SUM(v.treatment_count) AS treatment_count,
                SUM(v.total_medical_fee) AS total_medical_fee
            FROM analytics_treatment_daily v
            LEFT JOIN (
                SELECT
                    COALESCE(d.department_name, t.treatment_dept, '미지정') AS department_name,
                    COUNT(DISTINCT t.patient_no) AS unique_patient_count
                FROM treatments t
                LEFT JOIN department d
                    ON d.id = t.department_id
                WHERE t.treatment_date IS NOT NULL
                  AND DATE(t.treatment_date) BETWEEN :startDate AND :endDate
                GROUP BY COALESCE(d.department_name, t.treatment_dept, '미지정')
            ) u
                ON u.department_name = v.department_name
            WHERE v.metric_date BETWEEN :startDate AND :endDate
            GROUP BY v.department_name, u.unique_patient_count
            ORDER BY total_medical_fee DESC, v.department_name ASC
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public MySqlTreatmentStatisticsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public ClickHouseTreatmentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        return namedParameterJdbcTemplate.queryForObject(
                TREATMENT_SUMMARY_SQL,
                createDateParams(startDate, endDate),
                (rs, rowNum) -> ClickHouseTreatmentSummaryResponse.builder()
                        .visitPatientCount(getLong(rs, "visit_patient_count"))
                        .uniquePatientCount(getLong(rs, "unique_patient_count"))
                        .treatmentCount(getLong(rs, "treatment_count"))
                        .totalMedicalFee(getLong(rs, "total_medical_fee"))
                        .build());
    }

    public List<ClickHouseDailyTreatmentStatisticsResponse> getDailyStatistics(LocalDate startDate, LocalDate endDate) {
        return namedParameterJdbcTemplate.query(
                TREATMENT_DAILY_SQL,
                createDateParams(startDate, endDate),
                treatmentDailyRowMapper());
    }

    public List<ClickHouseTreatmentDepartmentStatisticsResponse> getDepartmentStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        return namedParameterJdbcTemplate.query(
                TREATMENT_DEPARTMENT_SQL,
                createDateParams(startDate, endDate),
                treatmentDepartmentRowMapper());
    }

    private MapSqlParameterSource createDateParams(LocalDate startDate, LocalDate endDate) {
        return new MapSqlParameterSource()
                .addValue("startDate", Date.valueOf(startDate))
                .addValue("endDate", Date.valueOf(endDate));
    }

    private RowMapper<ClickHouseDailyTreatmentStatisticsResponse> treatmentDailyRowMapper() {
        return (rs, rowNum) -> ClickHouseDailyTreatmentStatisticsResponse.builder()
                .metricDate(rs.getDate("metric_date").toLocalDate())
                .visitPatientCount(getLong(rs, "visit_patient_count"))
                .uniquePatientCount(getLong(rs, "unique_patient_count"))
                .treatmentCount(getLong(rs, "treatment_count"))
                .totalMedicalFee(getLong(rs, "total_medical_fee"))
                .build();
    }

    private RowMapper<ClickHouseTreatmentDepartmentStatisticsResponse> treatmentDepartmentRowMapper() {
        return (rs, rowNum) -> ClickHouseTreatmentDepartmentStatisticsResponse.builder()
                .departmentName(rs.getString("department_name"))
                .visitPatientCount(getLong(rs, "visit_patient_count"))
                .uniquePatientCount(getLong(rs, "unique_patient_count"))
                .treatmentCount(getLong(rs, "treatment_count"))
                .totalMedicalFee(getLong(rs, "total_medical_fee"))
                .build();
    }

    private Long getLong(ResultSet resultSet, String columnName) throws SQLException {
        Object value = resultSet.getObject(columnName);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }
}
