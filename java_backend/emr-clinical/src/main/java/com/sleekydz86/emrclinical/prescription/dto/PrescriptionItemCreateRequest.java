package com.sleekydz86.emrclinical.prescription.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionItemCreateRequest {

    @NotBlank(message = "약물 코드는 필수입니다.")
    @Size(max = 50, message = "약물 코드는 최대 50자까지 가능합니다.")
    private String drugCode;

    @NotBlank(message = "약물명은 필수입니다.")
    @Size(max = 200, message = "약물명은 최대 200자까지 가능합니다.")
    private String drugName;

    @NotBlank(message = "용법은 필수입니다.")
    @Size(max = 500, message = "용법은 최대 500자까지 가능합니다.")
    private String dosage;

    @NotBlank(message = "용량은 필수입니다.")
    @Size(max = 100, message = "용량은 최대 100자까지 가능합니다.")
    private String dose;

    @NotNull(message = "횟수는 필수입니다.")
    @Min(value = 1, message = "횟수는 최소 1회 이상이어야 합니다.")
    @Max(value = 10, message = "횟수는 최대 10회까지 가능합니다.")
    private Integer frequency;

    @NotNull(message = "일수는 필수입니다.")
    @Min(value = 1, message = "일수는 최소 1일 이상이어야 합니다.")
    @Max(value = 365, message = "일수는 최대 365일까지 가능합니다.")
    private Integer days;

    @NotNull(message = "총 수량은 필수입니다.")
    @Min(value = 1, message = "총 수량은 최소 1개 이상이어야 합니다.")
    private Integer totalQuantity;

    @NotBlank(message = "단위는 필수입니다.")
    @Size(max = 20, message = "단위는 최대 20자까지 가능합니다.")
    private String unit;

    private String specialNote;
}

