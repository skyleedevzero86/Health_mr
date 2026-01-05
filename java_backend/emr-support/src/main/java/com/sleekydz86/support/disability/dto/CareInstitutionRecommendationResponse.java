package com.sleekydz86.support.disability.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CareInstitutionRecommendationResponse {
    private Long institutionId;
    private String institutionName;
    private String institutionType;
    private String serviceType;
    private String address;
    private Double distance;
    private Double recommendationScore;
}

