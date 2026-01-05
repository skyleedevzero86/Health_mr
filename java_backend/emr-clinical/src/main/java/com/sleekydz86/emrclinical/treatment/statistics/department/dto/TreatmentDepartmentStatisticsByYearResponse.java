package com.sleekydz86.emrclinical.treatment.statistics.department.dto;

import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TreatmentDepartmentStatisticsByYearResponse {
    private String year;
    private Map<String, Object> departmentStatistics;
    private List<PublicStatistics> publicStatistics;

    @Getter
    @Builder
    public static class PublicStatistics {
        private String departmentName;
        private String regionCode;
        private String regionName;
        private Long patientCount;
        private Long treatmentCount;
        private Long medicalFee;
        private Long benefitFee;

        public static List<PublicStatistics> from(List<TreatmentDepartmentStatisticsEntity> entities) {
            return entities.stream()
                    .map(entity -> PublicStatistics.builder()
                            .departmentName(entity.getDepartmentName())
                            .regionCode(entity.getRegionCode())
                            .regionName(entity.getRegionName())
                            .patientCount(entity.getPatientCount())
                            .treatmentCount(entity.getTreatmentCount())
                            .medicalFee(entity.getMedicalFee())
                            .benefitFee(entity.getBenefitFee())
                            .build())
                    .toList();
        }
    }
}

