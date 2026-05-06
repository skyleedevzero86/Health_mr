package com.sleekydz86.finance.payment.statistics.clickhouse.repository;

import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHouseDailyPaymentStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentStatusStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentSummaryResponse;
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
public class ClickHousePaymentStatisticsRepository {

    private static final String PAYMENT_SUMMARY_SQL = """
            SELECT
                coalesce(sum(payment_count), 0) AS payment_count,
                coalesce(sum(total_amount), 0) AS total_amount,
                coalesce(sum(unpaid_amount), 0) AS unpaid_amount
            FROM analytics_payment_daily
            WHERE metric_date BETWEEN :startDate AND :endDate
            """;

    private static final String PAYMENT_DAILY_SQL = """
            SELECT
                metric_date,
                sum(payment_count) AS payment_count,
                sum(total_amount) AS total_amount,
                sum(unpaid_amount) AS unpaid_amount
            FROM analytics_payment_daily
            WHERE metric_date BETWEEN :startDate AND :endDate
            GROUP BY metric_date
            ORDER BY metric_date
            """;

    private static final String PAYMENT_STATUS_SQL = """
            SELECT
                payment_status,
                sum(payment_count) AS payment_count,
                sum(total_amount) AS total_amount,
                sum(unpaid_amount) AS unpaid_amount
            FROM analytics_payment_daily
            WHERE metric_date BETWEEN :startDate AND :endDate
            GROUP BY payment_status
            ORDER BY total_amount DESC, payment_status ASC
            """;

    private final NamedParameterJdbcTemplate clickhouseNamedParameterJdbcTemplate;

    public ClickHousePaymentStatisticsRepository(
            @Qualifier("paymentClickhouseNamedParameterJdbcTemplate")
            NamedParameterJdbcTemplate clickhouseNamedParameterJdbcTemplate) {
        this.clickhouseNamedParameterJdbcTemplate = clickhouseNamedParameterJdbcTemplate;
    }

    public ClickHousePaymentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        return clickhouseNamedParameterJdbcTemplate.queryForObject(
                PAYMENT_SUMMARY_SQL,
                createDateParams(startDate, endDate),
                (rs, rowNum) -> ClickHousePaymentSummaryResponse.builder()
                        .paymentCount(getLong(rs, "payment_count"))
                        .totalAmount(getLong(rs, "total_amount"))
                        .unpaidAmount(getLong(rs, "unpaid_amount"))
                        .build());
    }

    public List<ClickHouseDailyPaymentStatisticsResponse> getDailyStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        return clickhouseNamedParameterJdbcTemplate.query(
                PAYMENT_DAILY_SQL,
                createDateParams(startDate, endDate),
                paymentDailyRowMapper());
    }

    public List<ClickHousePaymentStatusStatisticsResponse> getStatusStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        return clickhouseNamedParameterJdbcTemplate.query(
                PAYMENT_STATUS_SQL,
                createDateParams(startDate, endDate),
                paymentStatusRowMapper());
    }

    private MapSqlParameterSource createDateParams(LocalDate startDate, LocalDate endDate) {
        return new MapSqlParameterSource()
                .addValue("startDate", Date.valueOf(startDate))
                .addValue("endDate", Date.valueOf(endDate));
    }

    private RowMapper<ClickHouseDailyPaymentStatisticsResponse> paymentDailyRowMapper() {
        return (rs, rowNum) -> ClickHouseDailyPaymentStatisticsResponse.builder()
                .metricDate(rs.getDate("metric_date").toLocalDate())
                .paymentCount(getLong(rs, "payment_count"))
                .totalAmount(getLong(rs, "total_amount"))
                .unpaidAmount(getLong(rs, "unpaid_amount"))
                .build();
    }

    private RowMapper<ClickHousePaymentStatusStatisticsResponse> paymentStatusRowMapper() {
        return (rs, rowNum) -> ClickHousePaymentStatusStatisticsResponse.builder()
                .paymentStatus(rs.getString("payment_status"))
                .paymentCount(getLong(rs, "payment_count"))
                .totalAmount(getLong(rs, "total_amount"))
                .unpaidAmount(getLong(rs, "unpaid_amount"))
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
