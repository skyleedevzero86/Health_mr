package com.sleekydz86.finance.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRelaySearchRequest {

    private Long patientNo;
    private Long contractCode;
    private Boolean isActive;
    private Integer page = 0;
    private Integer size = 20;
}
