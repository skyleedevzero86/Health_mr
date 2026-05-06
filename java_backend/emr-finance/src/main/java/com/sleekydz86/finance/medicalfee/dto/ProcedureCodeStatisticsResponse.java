package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProcedureCodeStatisticsResponse {
    private String procedureCode;
    private String startYear;
    private String endYear;
    private Long totalPatients;
    private Long totalTreatments;
    private List<YearStatistics> yearStatistics;
    private List<InstitutionStatistics> institutionStatistics;

    @Getter
    @Builder
    public static class YearStatistics {
        private String year;
        private Long patientCount;
        private Long treatmentCount;
        private Map<String, InstitutionStatistics> byInstitution;
    }

    @Getter
    @Builder
    public static class InstitutionStatistics {
        private String institutionType;
        private Long patientCount;
        private Long treatmentCount;
        private Double averageTreatmentsPerPatient;
    }
}

