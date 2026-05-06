package com.sleekydz86.emrclinical.ai.dto;

import com.sleekydz86.emrclinical.types.TreatmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRecommendationRequest {
    
    @NotNull(message = "진료 유형은 필수입니다.")
    private TreatmentType treatmentType;
    
    @NotBlank(message = "진료과는 필수입니다.")
    private String department;
    
    private String urgency;
}

