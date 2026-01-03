package com.sleekydz86.finance.contract.dto;

import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRelayDetailResponse {

    private Long contractRelayId;
    private Long patientNo;
    private String patientName;
    private Long contractCode;
    private String contractName;
    private Boolean isActive;
    private LocalDate relayStartDate;
    private LocalDate relayEndDate;

    public static ContractRelayDetailResponse from(ContractRelayEntity entity) {
        ContractRelayDetailResponse response = new ContractRelayDetailResponse();
        response.setContractRelayId(entity.getContractRelayId());
        response.setPatientNo(entity.getPatientEntity().getPatientNo());
        response.setPatientName(entity.getPatientEntity().getPatientName());
        response.setContractCode(entity.getContractEntity().getContractCode());
        response.setContractName(entity.getContractEntity().getContractName());
        response.setIsActive(entity.getIsActive());
        response.setRelayStartDate(entity.getRelayStartDate());
        response.setRelayEndDate(entity.getRelayEndDate());
        return response;
    }
}

