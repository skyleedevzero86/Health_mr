package com.sleekydz86.emrclinical.treatment.statistics.department.dto;

import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TreatmentDepartmentStatisticsByRegionResponse {
    private String year;
    private String regionCode;
    private Map<String, Object> internalStatistics;
    private List<PublicStatistics> publicStatistics;

    @Getter
    @Builder
    public static class PublicStatistics {
        private String departmentName;
        private Long patientCount;
        private Long treatmentCount;
        private Long medicalFee;
        private Long benefitFee;

        public static List<PublicStatistics> from(List<TreatmentDepartmentStatisticsEntity> entities) {
            return entities.stream()
                    .map(entity -> PublicStatistics.builder()
                            .departmentName(entity.getDepartmentName())
                            .patientCount(entity.getPatientCount())
                            .treatmentCount(entity.getTreatmentCount())
                            .medicalFee(entity.getMedicalFee())
                            .benefitFee(entity.getBenefitFee())
                            .build())
                    .toList();
        }
    }
}

