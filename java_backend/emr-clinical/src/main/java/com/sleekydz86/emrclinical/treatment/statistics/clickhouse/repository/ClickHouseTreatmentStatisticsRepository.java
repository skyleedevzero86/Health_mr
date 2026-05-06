package com.sleekydz86.emrclinical.treatment.statistics.clickhouse.repository;

import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Profile("clickhouse")
public class ClickHouseTreatmentStatisticsRepository {

    private static final String TREATMENT_SUMMARY_SQL = """
            SELECT
                coalesce(sum(patient_count), 0) AS patient_count,
                coalesce(sum(treatment_count), 0) AS treatment_count,
                coalesce(sum(total_medical_fee), 0) AS total_medical_fee
            FROM analytics_treatment_daily
            WHERE metric_date BETWEEN :startDate AND :endDate
            """;

    private static final String TREATMENT_DAILY_SQL = """
            SELECT
                metric_date,
                sum(patient_count) AS patient_count,
                sum(treatment_count) AS treatment_count,
                sum(total_medical_fee) AS total_medical_fee
            FROM analytics_treatment_daily
            WHERE metric_date BETWEEN :startDate AND :endDate
            GROUP BY metric_date
            ORDER BY metric_date
            """;

    private static final String TREATMENT_DEPARTMENT_SQL = """
            SELECT
                department_name,
                sum(patient_count) AS patient_count,
                sum(treatment_count) AS treatment_count,
                sum(total_medical_fee) AS total_medical_fee
            FROM analytics_treatment_daily
            WHERE metric_date BETWEEN :startDate AND :endDate
            GROUP BY department_name
            ORDER BY total_medical_fee DESC, department_name ASC
            """;

    private final NamedParameterJdbcTemplate clickhouseNamedParameterJdbcTemplate;

    public ClickHouseTreatmentStatisticsRepository(
            @Qualifier("treatmentClickhouseNamedParameterJdbcTemplate")
            NamedParameterJdbcTemplate clickhouseNamedParameterJdbcTemplate) {
        this.clickhouseNamedParameterJdbcTemplate = clickhouseNamedParameterJdbcTemplate;
    }

    public ClickHouseTreatmentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        return clickhouseNamedParameterJdbcTemplate.queryForObject(
                TREATMENT_SUMMARY_SQL,
                createDateParams(startDate, endDate),
                (rs, rowNum) -> ClickHouseTreatmentSummaryResponse.builder()
                        .patientCount(getLong(rs, "patient_count"))
                        .treatmentCount(getLong(rs, "treatment_count"))
                        .totalMedicalFee(getLong(rs, "total_medical_fee"))
                        .build());
    }

    public List<ClickHouseDailyTreatmentStatisticsResponse> getDailyStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        return clickhouseNamedParameterJdbcTemplate.query(
                TREATMENT_DAILY_SQL,
                createDateParams(startDate, endDate),
                treatmentDailyRowMapper());
    }

    public List<ClickHouseTreatmentDepartmentStatisticsResponse> getDepartmentStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        return clickhouseNamedParameterJdbcTemplate.query(
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
                .patientCount(getLong(rs, "patient_count"))
                .treatmentCount(getLong(rs, "treatment_count"))
                .totalMedicalFee(getLong(rs, "total_medical_fee"))
                .build();
    }

    private RowMapper<ClickHouseTreatmentDepartmentStatisticsResponse> treatmentDepartmentRowMapper() {
        return (rs, rowNum) -> ClickHouseTreatmentDepartmentStatisticsResponse.builder()
                .departmentName(rs.getString("department_name"))
                .patientCount(getLong(rs, "patient_count"))
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
