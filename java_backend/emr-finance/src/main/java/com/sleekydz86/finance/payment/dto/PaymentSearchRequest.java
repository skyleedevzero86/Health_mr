package com.sleekydz86.finance.payment.dto;

import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchRequest {

    private Long patientNo;
    private Long treatmentId;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDate startDate;
    private LocalDate endDate;
}

