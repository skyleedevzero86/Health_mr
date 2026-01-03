package com.sleekydz86.finance.contract.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRelayRequest {

    @NotNull(message = "환자 번호는 필수입니다.")
    private Long patientNo;

    @NotNull(message = "계약처 코드는 필수입니다.")
    private Long contractCode;
}
