package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ProcedureCodeStatisticsByYearResponse {
    private String procedureCode;
    private String year;
    private Long totalPatients;
    private Long totalTreatments;
    private Map<String, InstitutionStatistics> institutionStatistics;
    
    @Getter
    @Builder
    public static class InstitutionStatistics {
        private Long patientCount;
        private Long treatmentCount;
        private Double averageTreatmentsPerPatient;
    }
}

