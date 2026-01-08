package com.sleekydz86.emrclinical.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryAnalysisResponse {
    
    private Long patientNo;
    private String patientName;
    private Integer totalTreatments;
    private LocalDate firstVisit;
    private LocalDate lastVisit;
    private Map<String, Integer> typeDistribution;
    private Map<String, Integer> departmentDistribution;
    private Map<String, Integer> doctorDistribution;
    private RevisitPattern revisitPattern;
    private List<Insight> insights;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevisitPattern {
        private String frequency;
        private Integer averageDays;
        private List<Double> intervals;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insight {
        private String type;
        private String message;
    }
}

