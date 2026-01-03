package com.sleekydz86.finance.contract.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractUpdateRequest {

    private Long contractCode;

    @NotBlank(message = "계약처명은 필수입니다.")
    private String contractName;

    @NotBlank(message = "계약 관계는 필수입니다.")
    private String contractRelationship;

    private String contractTelephone;

    @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
    @Max(value = 100, message = "할인율은 100 이하여야 합니다.")
    private Long contractDiscount;

    private String contractManager;
    private String contractManagerTel;
    private String contractManagerEmail;
}
