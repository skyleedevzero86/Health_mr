package com.sleekydz86.emrclinical.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentReportRequest {
    
    @NotBlank(message = "보고서 유형은 필수입니다.")
    private String reportType;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String format;
}

