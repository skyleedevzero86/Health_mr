package com.sleekydz86.emrclinical.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    @Size(max = 500, message = "취소 사유는 최대 500자까지 가능합니다.")
    private String cancelReason;
}