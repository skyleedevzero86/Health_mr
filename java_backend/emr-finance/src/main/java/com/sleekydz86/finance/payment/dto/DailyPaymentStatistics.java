package com.sleekydz86.finance.payment.dto;

import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.Map;

@Builder
@Getter
public class DailyPaymentStatistics {
    private LocalDate date;
    private Long count;
    private Long totalAmount;
    private Long paidAmount;
    private Map<PaymentMethod, Long> methodStatistics;
    private Map<PaymentStatus, Long> statusStatistics;
}

