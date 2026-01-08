package com.sleekydz86.emrclinical.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentRecommendationRequest {
    
    @NotBlank(message = "증상 설명은 필수입니다.")
    private String symptoms;
    
    private Long patientNo;
    
    private String patientHistory;
}

