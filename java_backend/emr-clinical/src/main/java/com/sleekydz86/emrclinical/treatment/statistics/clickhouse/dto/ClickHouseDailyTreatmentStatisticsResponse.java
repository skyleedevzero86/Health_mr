package com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ClickHouseDailyTreatmentStatisticsResponse {

    private LocalDate metricDate;
    private Long patientCount;
    private Long treatmentCount;
    private Long totalMedicalFee;
}
