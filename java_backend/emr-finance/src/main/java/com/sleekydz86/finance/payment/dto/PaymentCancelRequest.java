package com.sleekydz86.finance.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}
