package com.sleekydz86.finance.payment.statistics.clickhouse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClickHousePaymentSummaryResponse {

    private Long paymentCount;
    private Long totalAmount;
    private Long unpaidAmount;
}
