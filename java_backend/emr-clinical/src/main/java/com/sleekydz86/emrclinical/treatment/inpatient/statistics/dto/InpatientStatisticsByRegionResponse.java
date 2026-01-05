package com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto;

import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class InpatientStatisticsByRegionResponse {
    private String year;
    private String regionCode;
    private Map<String, Object> internalStatistics;
    private List<PublicStatistics> publicStatistics;

    @Getter
    @Builder
    public static class PublicStatistics {
        private String institutionType;
        private Long visitDays;
        private Long benefitDays;
        private Long medicalFee;
        private Long benefitFee;

        public static List<PublicStatistics> from(List<InpatientStatisticsEntity> entities) {
            return entities.stream()
                    .map(entity -> PublicStatistics.builder()
                            .institutionType(entity.getInstitutionType())
                            .visitDays(entity.getVisitDays())
                            .benefitDays(entity.getBenefitDays())
                            .medicalFee(entity.getMedicalFee())
                            .benefitFee(entity.getBenefitFee())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}

