package com.sleekydz86.finance.payment.statistics.analytics.service;

import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHouseDailyPaymentStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentStatusStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface PaymentAnalyticsStatisticsService {

    String getSourceDatabase();

    String getAuditActionType();

    ClickHousePaymentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate);

    List<ClickHouseDailyPaymentStatisticsResponse> getDailyStatistics(LocalDate startDate, LocalDate endDate);

    List<ClickHousePaymentStatusStatisticsResponse> getStatusStatistics(LocalDate startDate, LocalDate endDate);

    LocalDate resolveStartDate(LocalDate startDate, LocalDate endDate);

    LocalDate resolveEndDate(LocalDate startDate, LocalDate endDate);
}
