package com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto;

import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class InpatientStatisticsResponse {
    private String year;
    private String institutionType;
    private String regionCode;
    private Map<String, Object> internalStatistics;
    private List<PublicStatistics> publicStatistics;
    private Map<String, Object> comparison;

    @Getter
    @Builder
    public static class PublicStatistics {
        private Long statisticsId;
        private String statisticsYear;
        private String institutionType;
        private String regionCode;
        private String regionName;
        private Long visitDays;
        private Long benefitDays;
        private Long medicalFee;
        private Long benefitFee;

        public static PublicStatistics from(InpatientStatisticsEntity entity) {
            return PublicStatistics.builder()
                    .statisticsId(entity.getStatisticsId())
                    .statisticsYear(entity.getStatisticsYear())
                    .institutionType(entity.getInstitutionType())
                    .regionCode(entity.getRegionCode())
                    .regionName(entity.getRegionName())
                    .visitDays(entity.getVisitDays())
                    .benefitDays(entity.getBenefitDays())
                    .medicalFee(entity.getMedicalFee())
                    .benefitFee(entity.getBenefitFee())
                    .build();
        }
    }
}

