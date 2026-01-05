package com.sleekydz86.support.healthcheckup.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthCheckupInstitutionRecommendationResponse {
    private Long institutionId;
    private String institutionName;
    private String institutionType;
    private String address;
    private String sido;
    private String sigungu;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private Double distance;
    private Double recommendationScore;
}

