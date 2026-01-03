package com.sleekydz86.finance.contract.dto;

import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRelayResponse {

    private Long contractRelayId;
    private Long patientNo;
    private String patientName;
    private Long contractCode;
    private String contractName;
    private Boolean isActive;

    public static ContractRelayResponse from(ContractRelayEntity entity) {
        ContractRelayResponse response = new ContractRelayResponse();
        response.setContractRelayId(entity.getContractRelayId());
        response.setPatientNo(entity.getPatientEntity().getPatientNo().getValue());
        response.setPatientName(entity.getPatientEntity().getPatientName());
        response.setContractCode(entity.getContractEntity().getContractCode());
        response.setContractName(entity.getContractEntity().getContractName());
        response.setIsActive(entity.getIsActive());
        return response;
    }
}

