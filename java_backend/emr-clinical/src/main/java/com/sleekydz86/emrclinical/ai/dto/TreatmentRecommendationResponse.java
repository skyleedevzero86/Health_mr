package com.sleekydz86.emrclinical.ai.dto;

import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentRecommendationResponse {
    
    private TreatmentType recommendedType;
    private Double confidence;
    private String reason;
    private List<Alternative> alternatives;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alternative {
        private TreatmentType type;
        private Double confidence;
        private String reason;
    }
}

