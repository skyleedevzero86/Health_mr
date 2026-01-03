package com.sleekydz86.finance.payment.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Builder
@Getter
public class UnpaidPaymentStatistics {
    private Long count;
    private Long totalUnpaidAmount;
    private Map<Long, Long> patientStatistics;
}

