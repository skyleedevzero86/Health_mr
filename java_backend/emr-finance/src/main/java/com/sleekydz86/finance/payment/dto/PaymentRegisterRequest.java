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
public class PaymentRegisterRequest {

    @NotNull(message = "진료 ID는 필수입니다.")
    private Long treatmentId;

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;
}
