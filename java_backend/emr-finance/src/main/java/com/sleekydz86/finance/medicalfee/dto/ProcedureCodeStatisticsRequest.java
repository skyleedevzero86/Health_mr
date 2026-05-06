package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProcedureCodeStatisticsRequest {
    private String procedureCode;
    private String startYear;
    private String endYear;
    private String institutionType;
}

