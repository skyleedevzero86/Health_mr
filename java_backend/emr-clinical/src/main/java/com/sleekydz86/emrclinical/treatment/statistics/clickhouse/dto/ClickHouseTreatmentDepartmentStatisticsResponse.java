package com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClickHouseTreatmentDepartmentStatisticsResponse {

    private String departmentName;
    private Long patientCount;
    private Long treatmentCount;
    private Long totalMedicalFee;
}
