package com.sleekydz86.finance.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentCalculationResult {
    private Long totalAmount;
    private Long selfPay;
    private Long insuranceMoney;
    private Long discountAmount;
}

