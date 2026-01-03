package com.sleekydz86.finance.medicalfee.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalFeeUpdateRequest {

    private Long medicalTypeId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    private Long medicalFeeAmount;
}
