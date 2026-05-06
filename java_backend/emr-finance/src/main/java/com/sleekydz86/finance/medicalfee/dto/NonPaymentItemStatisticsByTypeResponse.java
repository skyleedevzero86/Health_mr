package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class NonPaymentItemStatisticsByTypeResponse {
    private String npayCd;
    private String npayKorNm;
    private LocalDate stdDate;
    
    private PriceStatistics all;
    private PriceStatistics usgh;
    private PriceStatistics gnhp;
    private PriceStatistics hosp;
    private PriceStatistics cmdc;
    private PriceStatistics dety;
    private PriceStatistics recu;
    
    @Getter
    @Builder
    public static class PriceStatistics {
        private Long minPrice;
        private Long maxPrice;
        private Long avgPrice;
        private Long middlePrice;
    }
}

