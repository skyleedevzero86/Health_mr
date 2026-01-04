package com.sleekydz86.support.disability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisabilityRegisterRequest {
    @NotNull(message = "환자번호는 필수 값입니다.")
    private Long patientNo;
    @NotBlank(message = "장애 등급은 필수 값입니다.")
    private String disabilityGrade;
    @NotBlank(message = "장애 유형은 필수 값입니다.")
    private String disabilityType;
    @NotBlank(message = "보조기기 필요 여부는 필수 값입니다.")
    private String disabilityDeviceYN;
    private String disabilityDeviceType;
}