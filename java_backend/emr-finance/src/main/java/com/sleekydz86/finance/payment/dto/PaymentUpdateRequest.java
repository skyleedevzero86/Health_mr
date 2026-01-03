package com.sleekydz86.finance.payment.dto;

import com.sleekydz86.finance.type.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUpdateRequest {

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다.")
    private Long paymentAmount;

    private PaymentMethod paymentMethod;
}

