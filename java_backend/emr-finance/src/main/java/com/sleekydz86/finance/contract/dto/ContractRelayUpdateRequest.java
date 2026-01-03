package com.sleekydz86.finance.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRelayUpdateRequest {

    private Boolean isActive;
    private LocalDate relayStartDate;
    private LocalDate relayEndDate;
}

