package com.sleekydz86.finance.contract.dto;

import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.type.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractDetailResponse {

    private Long contractCode;
    private String contractName;
    private String contractRelationship;
    private String contractTelephone;
    private Long contractDiscount;
    private ContractStatus contractStatus;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractManager;
    private String contractManagerTel;
    private String contractManagerEmail;

    public static ContractDetailResponse from(ContractEntity entity) {
        ContractDetailResponse response = new ContractDetailResponse();
        response.setContractCode(entity.getContractCode());
        response.setContractName(entity.getContractName());
        response.setContractRelationship(entity.getContractRelationship());
        response.setContractTelephone(entity.getContractTelephoneValue());
        response.setContractDiscount(entity.getContractDiscount());
        response.setContractStatus(entity.getContractStatus());
        response.setContractStartDate(entity.getContractStartDate());
        response.setContractEndDate(entity.getContractEndDate());
        response.setContractManager(entity.getContractManager());
        response.setContractManagerTel(entity.getContractManagerTelValue());
        response.setContractManagerEmail(entity.getContractManagerEmailValue());
        return response;
    }
}

