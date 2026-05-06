package com.sleekydz86.finance.payment.statistics.clickhouse.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ClickHouseDailyPaymentStatisticsResponse {

    private LocalDate metricDate;
    private Long paymentCount;
    private Long totalAmount;
    private Long unpaidAmount;
}
