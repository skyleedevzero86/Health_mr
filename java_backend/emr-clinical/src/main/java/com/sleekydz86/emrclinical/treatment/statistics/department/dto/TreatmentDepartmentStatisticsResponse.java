package com.sleekydz86.emrclinical.treatment.statistics.department.dto;

import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TreatmentDepartmentStatisticsResponse {
    private String year;
    private String departmentName;
    private String regionCode;
    private Map<String, Object> internalStatistics;
    private List<PublicStatistics> publicStatistics;
    private Map<String, Object> comparison;

    @Getter
    @Builder
    public static class PublicStatistics {
        private Long statisticsId;
        private String statisticsYear;
        private String regionCode;
        private String regionName;
        private String departmentName;
        private Long patientCount;
        private Long treatmentCount;
        private Long medicalFee;
        private Long benefitFee;

        public static PublicStatistics from(TreatmentDepartmentStatisticsEntity entity) {
            return PublicStatistics.builder()
                    .statisticsId(entity.getStatisticsId())
                    .statisticsYear(entity.getStatisticsYear())
                    .regionCode(entity.getRegionCode())
                    .regionName(entity.getRegionName())
                    .departmentName(entity.getDepartmentName())
                    .patientCount(entity.getPatientCount())
                    .treatmentCount(entity.getTreatmentCount())
                    .medicalFee(entity.getMedicalFee())
                    .benefitFee(entity.getBenefitFee())
                    .build();
        }
    }
}

