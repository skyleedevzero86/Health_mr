package com.sleekydz86.emrclinical.treatment.statistics.department.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TreatmentDepartmentStatisticsRequest {
    private String year;
    private String departmentName;
    private String regionCode;
}

