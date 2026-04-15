package com.sleekydz86.emrclinical.treatment.statistics.mysql.service;

import com.sleekydz86.core.common.exception.ErrorCode;
import com.sleekydz86.core.common.exception.custom.BaseException;
import com.sleekydz86.emrclinical.treatment.statistics.analytics.service.TreatmentAnalyticsStatisticsService;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;
import com.sleekydz86.emrclinical.treatment.statistics.mysql.repository.MySqlTreatmentStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Profile("!clickhouse")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MySqlTreatmentStatisticsService implements TreatmentAnalyticsStatisticsService {

    private static final int DEFAULT_RANGE_DAYS = 7;
    private static final int MAX_RANGE_DAYS = 90;
    private static final String PROD_SCRIPT_PATH = "db/mysql/prod/analytics_treatment_daily.sql";
    private static final String INVALID_RANGE_MESSAGE =
            "\uC2DC\uC791\uC77C\uC740 \uC885\uB8CC\uC77C\uBCF4\uB2E4 \uB2A6\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.";
    private static final String RANGE_LIMIT_PREFIX =
            "\uC870\uD68C \uAE30\uAC04\uC740 \uCD5C\uB300 ";
    private static final String RANGE_LIMIT_SUFFIX =
            "\uC77C\uAE4C\uC9C0 \uAC00\uB2A5\uD569\uB2C8\uB2E4.";
    private static final String MISSING_VIEW_PREFIX =
            "\uC6B4\uC601 \uD1B5\uACC4\uC6A9 MySQL \uBDF0\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4. `";
    private static final String MISSING_VIEW_SUFFIX =
            "` \uC2A4\uD06C\uB9BD\uD2B8\uB97C \uBA3C\uC800 \uC801\uC6A9\uD574\uC8FC\uC138\uC694.";

    private final MySqlTreatmentStatisticsRepository mySqlTreatmentStatisticsRepository;

    @Override
    public String getSourceDatabase() {
        return "MYSQL";
    }

    @Override
    public String getAuditActionType() {
        return "MYSQL_ANALYTICS_USAGE";
    }

    @Override
    public ClickHouseTreatmentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        try {
            return mySqlTreatmentStatisticsRepository.getSummary(dateRange.startDate(), dateRange.endDate());
        } catch (DataAccessException exception) {
            throw createMissingViewException(PROD_SCRIPT_PATH, exception);
        }
    }

    @Override
    public List<ClickHouseDailyTreatmentStatisticsResponse> getDailyStatistics(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        try {
            return mySqlTreatmentStatisticsRepository.getDailyStatistics(dateRange.startDate(), dateRange.endDate());
        } catch (DataAccessException exception) {
            throw createMissingViewException(PROD_SCRIPT_PATH, exception);
        }
    }

    @Override
    public List<ClickHouseTreatmentDepartmentStatisticsResponse> getDepartmentStatistics(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        try {
            return mySqlTreatmentStatisticsRepository.getDepartmentStatistics(dateRange.startDate(), dateRange.endDate());
        } catch (DataAccessException exception) {
            throw createMissingViewException(PROD_SCRIPT_PATH, exception);
        }
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
                : resolvedEndDate.minusDays(DEFAULT_RANGE_DAYS - 1L);

        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, INVALID_RANGE_MESSAGE);
        }

        long totalDays = ChronoUnit.DAYS.between(resolvedStartDate, resolvedEndDate) + 1;
        if (totalDays > MAX_RANGE_DAYS) {
            throw new BaseException(
                    ErrorCode.BAD_REQUEST,
                    RANGE_LIMIT_PREFIX + MAX_RANGE_DAYS + RANGE_LIMIT_SUFFIX);
        }

        return new DateRange(resolvedStartDate, resolvedEndDate);
    }

    private BaseException createMissingViewException(String scriptPath, Exception cause) {
        return new BaseException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                MISSING_VIEW_PREFIX + scriptPath + MISSING_VIEW_SUFFIX,
                cause);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
