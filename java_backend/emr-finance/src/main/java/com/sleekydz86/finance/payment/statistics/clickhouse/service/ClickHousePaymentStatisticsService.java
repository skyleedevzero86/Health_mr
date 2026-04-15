package com.sleekydz86.finance.payment.statistics.clickhouse.service;

import com.sleekydz86.core.common.exception.ErrorCode;
import com.sleekydz86.core.common.exception.custom.BaseException;
import com.sleekydz86.finance.config.PaymentClickHouseStatisticsProperties;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHouseDailyPaymentStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentStatusStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentSummaryResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.repository.ClickHousePaymentStatisticsRepository;
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
public class ClickHousePaymentStatisticsService {

    private final ClickHousePaymentStatisticsRepository clickHousePaymentStatisticsRepository;
    private final PaymentClickHouseStatisticsProperties clickHouseStatisticsProperties;

    public ClickHousePaymentSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        return clickHousePaymentStatisticsRepository.getSummary(dateRange.startDate(), dateRange.endDate());
    }

    public List<ClickHouseDailyPaymentStatisticsResponse> getDailyStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        return clickHousePaymentStatisticsRepository.getDailyStatistics(
                dateRange.startDate(), dateRange.endDate());
    }

    public List<ClickHousePaymentStatusStatisticsResponse> getStatusStatistics(
            LocalDate startDate,
            LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        return clickHousePaymentStatisticsRepository.getStatusStatistics(
                dateRange.startDate(), dateRange.endDate());
    }

    public LocalDate resolveStartDate(LocalDate startDate, LocalDate endDate) {
        return resolveDateRange(startDate, endDate).startDate();
    }

    public LocalDate resolveEndDate(LocalDate startDate, LocalDate endDate) {
        return resolveDateRange(startDate, endDate).endDate();
    }

    private DateRange resolveDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStartDate = startDate != null
                ? startDate
                : resolvedEndDate.minusDays(Math.max(clickHouseStatisticsProperties.getDefaultRangeDays(), 1) - 1L);

        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "시작일은 종료일보다 늦을 수 없습니다.");
        }

        long totalDays = ChronoUnit.DAYS.between(resolvedStartDate, resolvedEndDate) + 1;
        if (totalDays > clickHouseStatisticsProperties.getMaxRangeDays()) {
            throw new BaseException(
                    ErrorCode.BAD_REQUEST,
                    "조회 기간은 최대 " + clickHouseStatisticsProperties.getMaxRangeDays() + "일까지 가능합니다.");
        }

        return new DateRange(resolvedStartDate, resolvedEndDate);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
