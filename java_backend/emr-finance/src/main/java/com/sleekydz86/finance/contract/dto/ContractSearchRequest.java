package com.sleekydz86.finance.contract.dto;

import com.sleekydz86.finance.type.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractSearchRequest {

    private String keyword;
    private ContractStatus status;
    private Integer page = 0;
    private Integer size = 20;
}
