package com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InpatientStatisticsRequest {
    private String year;
    private String institutionType;
    private String regionCode;
}

