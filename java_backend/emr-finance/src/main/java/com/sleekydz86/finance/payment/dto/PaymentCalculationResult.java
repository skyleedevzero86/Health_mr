package com.sleekydz86.finance.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentCalculationResult {
    private Long totalAmount;      // 총 금액
    private Long selfPay;          // 본인 부담금
    private Long insuranceMoney;  // 보험사 지원금
    private Long discountAmount;  // 할인 금액
}

