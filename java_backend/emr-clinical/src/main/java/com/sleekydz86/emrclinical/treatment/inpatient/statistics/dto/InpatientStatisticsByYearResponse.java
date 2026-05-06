package com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class InpatientStatisticsByYearResponse {
    private String year;
    private Map<String, Object> inpatientStatistics;
    private Map<String, Object> outpatientStatistics;
    private Map<String, Object> prescriptionStatistics;
    private Map<String, Object> totalStatistics;
}

