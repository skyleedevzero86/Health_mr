package com.sleekydz86.support.disability.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DisabilityWithCareInstitutionResponse {
    private DisabilityResponse disabilityInfo;
    private List<CareInstitutionRecommendationResponse> recommendedInstitutions;
}

