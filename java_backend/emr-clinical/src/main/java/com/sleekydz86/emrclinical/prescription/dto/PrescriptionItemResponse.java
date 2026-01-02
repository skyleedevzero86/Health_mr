package com.sleekydz86.emrclinical.prescription.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrescriptionItemResponse {
    private Long prescriptionItemId;
    private String drugCode;
    private String drugName;
    private String dosage;
    private String dose;
    private Integer frequency;
    private Integer days;
    private Integer totalQuantity;
    private String unit;
    private String specialNote;
}