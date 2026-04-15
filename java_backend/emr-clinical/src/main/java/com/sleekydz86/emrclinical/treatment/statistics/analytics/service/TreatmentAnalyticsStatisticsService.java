package com.sleekydz86.emrclinical.treatment.statistics.analytics.service;

import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface TreatmentAnalyticsStatisticsService {

    String getSourceDatabase();

    String getAuditActionType();

    ClickHouseTreatmentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate);

    List<ClickHouseDailyTreatmentStatisticsResponse> getDailyStatistics(LocalDate startDate, LocalDate endDate);

    List<ClickHouseTreatmentDepartmentStatisticsResponse> getDepartmentStatistics(LocalDate startDate, LocalDate endDate);

    LocalDate resolveStartDate(LocalDate startDate, LocalDate endDate);

    LocalDate resolveEndDate(LocalDate startDate, LocalDate endDate);
}
