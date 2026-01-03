package com.sleekydz86.finance.payment.dto;

import com.sleekydz86.finance.type.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteRequest {

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    private String approvalNumber;

    private String cardCompany;
}