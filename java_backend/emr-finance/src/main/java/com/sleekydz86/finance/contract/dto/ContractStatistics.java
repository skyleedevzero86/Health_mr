package com.sleekydz86.finance.contract.dto;

import java.util.Map;

@lombok.Builder
@lombok.Getter
public class ContractStatistics {
    private Map<Long, Long> patientCountByContract;
    private Map<Long, Long> discountAmountByContract;
    private Map<Long, Long> usageFrequencyByContract;
}