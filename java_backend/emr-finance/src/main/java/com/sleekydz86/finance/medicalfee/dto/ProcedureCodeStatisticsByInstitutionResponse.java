package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ProcedureCodeStatisticsByInstitutionResponse {
    private String procedureCode;
    private String institutionType;
    private Long totalPatients;
    private Long totalTreatments;
    private Map<String, YearStatistics> yearStatistics;
    
    @Getter
    @Builder
    public static class YearStatistics {
        private Long patientCount;
        private Long treatmentCount;
        private Double averageTreatmentsPerPatient;
    }
}

