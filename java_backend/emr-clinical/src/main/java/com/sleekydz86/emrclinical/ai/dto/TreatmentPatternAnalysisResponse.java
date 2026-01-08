package com.sleekydz86.emrclinical.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPatternAnalysisResponse {
    
    private String period;
    private Integer totalTreatments;
    private Map<String, Integer> typeDistribution;
    private Map<String, Integer> departmentDistribution;
    private Map<String, Integer> doctorDistribution;
    private Map<String, Integer> dailyTrend;
    private List<Insight> insights;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insight {
        private String type;
        private String title;
        private String message;
    }
}

