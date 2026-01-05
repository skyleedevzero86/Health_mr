package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class NonPaymentItemStatisticsByRegionResponse {
    private String npayCd;
    private String npayKorNm;
    private LocalDate stdDate;
    private Map<String, PriceStatistics> regionStatistics;
    
    @Getter
    @Builder
    public static class PriceStatistics {
        private Long minPrice;
        private Long maxPrice;
        private Long avgPrice;
        private Long middlePrice;
    }
}

