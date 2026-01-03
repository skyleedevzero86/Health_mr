package com.sleekydz86.finance.medicalfee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalTypeRequest {

    @NotBlank(message = "진료 유형 코드는 필수입니다.")
    private String medicalTypeCode;

    @NotBlank(message = "진료 유형명은 필수입니다.")
    private String medicalTypeName;

    @NotNull(message = "진료 유형별 비용은 필수입니다.")
    @Positive(message = "진료 유형별 비용은 0보다 커야 합니다.")
    private Long medicalTypeFee;

    private String medicalTypeDescription;
}

