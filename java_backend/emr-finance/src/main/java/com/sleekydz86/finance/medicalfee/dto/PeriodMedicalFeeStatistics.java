package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.Map;

@Builder
@Getter
public class PeriodMedicalFeeStatistics {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long count;
    private Long totalAmount;
    private Map<String, Long> typeStatistics;
    private Map<String, Long> departmentStatistics;
}

