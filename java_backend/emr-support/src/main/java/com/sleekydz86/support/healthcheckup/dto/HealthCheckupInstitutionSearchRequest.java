package com.sleekydz86.support.healthcheckup.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthCheckupInstitutionSearchRequest {
    private String regionCode;
    private String institutionType;
    private String institutionName;
    private String sido;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}

