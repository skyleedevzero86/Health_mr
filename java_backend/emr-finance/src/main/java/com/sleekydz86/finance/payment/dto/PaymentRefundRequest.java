package com.sleekydz86.finance.payment.dto;

import com.sleekydz86.finance.type.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {

    @NotNull(message = "환불 금액은 필수입니다.")
    @Positive(message = "환불 금액은 0보다 커야 합니다.")
    private Long refundAmount;

    @NotNull(message = "환불 수단은 필수입니다.")
    private PaymentMethod refundMethod;
}

