package com.sleekydz86.finance.medicalfee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalFeeSearchRequest {

    private Long treatmentId;
    private Long medicalTypeId;
    private Integer page = 0;
    private Integer size = 20;
}