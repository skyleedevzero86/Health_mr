package com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClickHouseTreatmentSummaryResponse {

    private Long visitPatientCount;
    private Long uniquePatientCount;
    private Long treatmentCount;
    private Long totalMedicalFee;
}
