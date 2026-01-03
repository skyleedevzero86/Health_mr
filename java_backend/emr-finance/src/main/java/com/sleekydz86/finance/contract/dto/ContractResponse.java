package com.sleekydz86.finance.contract.dto;

import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.type.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {

    private Long contractCode;
    private String contractName;
    private String contractRelationship;
    private String contractTelephone;
    private Long contractDiscount;
    private ContractStatus contractStatus;
    private String contractManager;
    private String contractManagerTel;
    private String contractManagerEmail;

    public static ContractResponse from(ContractEntity entity) {
        ContractResponse response = new ContractResponse();
        response.setContractCode(entity.getContractCode());
        response.setContractName(entity.getContractName());
        response.setContractRelationship(entity.getContractRelationship());
        response.setContractTelephone(entity.getContractTelephoneValue());
        response.setContractDiscount(entity.getContractDiscount());
        response.setContractStatus(entity.getContractStatus());
        response.setContractManager(entity.getContractManager());
        response.setContractManagerTel(entity.getContractManagerTelValue());
        response.setContractManagerEmail(entity.getContractManagerEmailValue());
        return response;
    }
}

