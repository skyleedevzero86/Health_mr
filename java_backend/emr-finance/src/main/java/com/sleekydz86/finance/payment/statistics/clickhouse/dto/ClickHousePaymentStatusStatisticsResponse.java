package com.sleekydz86.finance.payment.statistics.clickhouse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClickHousePaymentStatusStatisticsResponse {

    private String paymentStatus;
    private Long paymentCount;
    private Long totalAmount;
    private Long unpaidAmount;
}
