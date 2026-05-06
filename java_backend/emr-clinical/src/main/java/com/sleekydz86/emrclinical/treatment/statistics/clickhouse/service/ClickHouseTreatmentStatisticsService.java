package com.sleekydz86.emrclinical.treatment.statistics.clickhouse.service;

import com.sleekydz86.core.common.exception.ErrorCode;
import com.sleekydz86.core.common.exception.custom.BaseException;
import com.sleekydz86.emrclinical.config.TreatmentClickHouseStatisticsProperties;
import com.sleekydz86.emrclinical.treatment.statistics.analytics.service.TreatmentAnalyticsStatisticsService;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.repository.ClickHouseTreatmentStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Profile("clickhouse")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClickHouseTreatmentStatisticsService implements TreatmentAnalyticsStatisticsService {

    private static final String INVALID_RANGE_MESSAGE =
            "\uC2DC\uC791\uC77C\uC740 \uC885\uB8CC\uC77C\uBCF4\uB2E4 \uB2A6\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.";
    private static final String RANGE_LIMIT_PREFIX =
            "\uC870\uD68C \uAE30\uAC04\uC740 \uCD5C\uB300 ";
    private static final String RANGE_LIMIT_SUFFIX =
            "\uC77C\uAE4C\uC9C0 \uAC00\uB2A5\uD569\uB2C8\uB2E4.";

    private final ClickHouseTreatmentStatisticsRepository clickHouseTreatmentStatisticsRepository;
    private final TreatmentClickHouseStatisticsProperties clickHouseStatisticsProperties;

    @Override
    public String getSourceDatabase() {
        return "CLICKHOUSE";
    }

    @Override
    public String getAuditActionType() {
        return "CLICKHOUSE_USAGE";
    }

    @Override
    public ClickHouseTreatmentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        return clickHouseTreatmentStatisticsRepository.getSummary(dateRange.startDate(), dateRange.endDate());
    }

    @Override
    public List<ClickHouseDailyTreatmentStatisticsResponse> getDailyStatistics(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        return clickHouseTreatmentStatisticsRepository.getDailyStatistics(dateRange.startDate(), dateRange.endDate());
    }

    @Override
    public List<ClickHouseTreatmentDepartmentStatisticsResponse> getDepartmentStatistics(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        return clickHouseTreatmentStatisticsRepository.getDepartmentStatistics(dateRange.startDate(), dateRange.endDate());
    }

    @Override
    public LocalDate resolveStartDate(LocalDate startDate, LocalDate endDate) {
        return resolveDateRange(startDate, endDate).startDate();
    }

    @Override
    public LocalDate resolveEndDate(LocalDate startDate, LocalDate endDate) {
        return resolveDateRange(startDate, endDate).endDate();
    }

    private DateRange resolveDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStartDate = startDate != null
                ? startDate
                : resolvedEndDate.minusDays(Math.max(clickHouseStatisticsProperties.getDefaultRangeDays(), 1) - 1L);

        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, INVALID_RANGE_MESSAGE);
        }

        long totalDays = ChronoUnit.DAYS.between(resolvedStartDate, resolvedEndDate) + 1;
        if (totalDays > clickHouseStatisticsProperties.getMaxRangeDays()) {
            throw new BaseException(
                    ErrorCode.BAD_REQUEST,
                    RANGE_LIMIT_PREFIX + clickHouseStatisticsProperties.getMaxRangeDays() + RANGE_LIMIT_SUFFIX);
        }

        return new DateRange(resolvedStartDate, resolvedEndDate);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
