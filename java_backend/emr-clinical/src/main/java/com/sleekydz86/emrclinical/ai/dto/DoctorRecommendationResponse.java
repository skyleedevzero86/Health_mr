package com.sleekydz86.emrclinical.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRecommendationResponse {
    
    private RecommendedDoctor recommended;
    private Double confidence;
    private String reason;
    private List<RecommendedDoctor> alternatives;
    private String message;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedDoctor {
        private Long userId;
        private String name;
        private String department;
        private Integer workload;
        private Integer experience;
    }
}

