package com.sleekydz86.finance.contract.service;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.finance.contract.dto.ContractDetailResponse;
import com.sleekydz86.finance.contract.dto.ContractRequest;
import com.sleekydz86.finance.contract.dto.ContractResponse;
import com.sleekydz86.finance.contract.dto.ContractUpdateRequest;
import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import com.sleekydz86.finance.contract.repository.ContractRelayRepository;
import com.sleekydz86.finance.contract.repository.ContractRepository;
import com.sleekydz86.finance.type.ContractStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractService implements BaseService<ContractEntity, Long> {

    private final ContractRepository contractRepository;
    private final ContractRelayRepository contractRelayRepository;

    public ContractResponse getContractByCode(Long contractCode) {
        ContractEntity contract = contractRepository.findByContractCode(contractCode)
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + contractCode));
        return ContractResponse.from(contract);
    }

    public ContractDetailResponse getContractDetailByCode(Long contractCode) {
        ContractEntity contract = contractRepository.findByContractCode(contractCode)
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + contractCode));
        return ContractDetailResponse.from(contract);
    }

    public Page<ContractResponse> getAllContracts(Pageable pageable) {
        Page<ContractEntity> contracts = contractRepository.findAll(pageable);
        return contracts.map(ContractResponse::from);
    }

    public Page<ContractResponse> getContractsByStatus(ContractStatus status, Pageable pageable) {
        Page<ContractEntity> contracts = contractRepository.findByContractStatus(status, pageable);
        return contracts.map(ContractResponse::from);
    }

    public List<ContractResponse> searchContracts(String keyword) {
        List<ContractEntity> contracts = contractRepository.searchContracts(keyword, ContractStatus.ACTIVE);
        return contracts.stream().map(ContractResponse::from).collect(Collectors.toList());
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ContractResponse createContract(ContractRequest request) {

        validateNotDuplicate(contractRepository.existsByContractCode(request.getContractCode()),
                "이미 존재하는 계약처 코드입니다: " + request.getContractCode());

        if (request.getContractDiscount() != null) {
            if (request.getContractDiscount() < 0 || request.getContractDiscount() > 100) {
                throw new BusinessException("할인율은 0 이상 100 이하여야 합니다.");
            }
        }

        ContractEntity contract = ContractEntity.builder()
                .contractCode(request.getContractCode())
                .contractName(request.getContractName())
                .contractRelationship(request.getContractRelationship())
                .contractTelephone(request.getContractTelephone())
                .contractDiscount(request.getContractDiscount())
                .contractStatus(ContractStatus.ACTIVE)
                .contractManager(request.getContractManager())
                .contractManagerTel(request.getContractManagerTel())
                .contractManagerEmail(request.getContractManagerEmail())
                .build();

        ContractEntity saved = contractRepository.save(contract);
        return ContractResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ContractResponse updateContract(Long contractCode, ContractUpdateRequest request) {
        ContractEntity contract = contractRepository.findByContractCode(contractCode)
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + contractCode));

        if (request.getContractCode() != null && !contract.getContractCode().equals(request.getContractCode())) {
            validateNotDuplicate(contractRepository.existsByContractCode(request.getContractCode()),
                    "이미 존재하는 계약처 코드입니다: " + request.getContractCode());
            contract.setContractCode(request.getContractCode());
        }

        if (request.getContractDiscount() != null) {
            if (request.getContractDiscount() < 0 || request.getContractDiscount() > 100) {
                throw new BusinessException("할인율은 0 이상 100 이하여야 합니다.");
            }
            contract.setContractDiscount(request.getContractDiscount());
        }

        if (request.getContractName() != null) {
            contract.setContractName(request.getContractName());
        }
        if (request.getContractRelationship() != null) {
            contract.setContractRelationship(request.getContractRelationship());
        }
        if (request.getContractTelephone() != null) {
            contract.setContractTelephone(request.getContractTelephone());
        }
        if (request.getContractManager() != null) {
            contract.setContractManager(request.getContractManager());
        }
        if (request.getContractManagerTel() != null) {
            contract.setContractManagerTel(request.getContractManagerTel());
        }
        if (request.getContractManagerEmail() != null) {
            contract.setContractManagerEmail(request.getContractManagerEmail());
        }

        ContractEntity saved = contractRepository.save(contract);
        return ContractResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.DELETE)
    public void deleteContract(Long contractCode) {
        ContractEntity contract = contractRepository.findByContractCode(contractCode)
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + contractCode));

        List<ContractRelayEntity> relays =
                contractRelayRepository.findByContractEntity_ContractCode(contractCode);
        if (!relays.isEmpty()) {
            throw new BusinessException("사용 중인 계약처는 삭제할 수 없습니다.");
        }

        contract.setContractStatus(ContractStatus.INACTIVE);
        contractRepository.save(contract);
    }

    @Transactional
    public void activateContract(Long contractCode) {
        ContractEntity contract = contractRepository.findByContractCode(contractCode)
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + contractCode));
        contract.setContractStatus(ContractStatus.ACTIVE);
        contractRepository.save(contract);
    }

    @Transactional
    public void deactivateContract(Long contractCode) {
        ContractEntity contract = contractRepository.findByContractCode(contractCode)
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + contractCode));
        contract.setContractStatus(ContractStatus.INACTIVE);
        contractRepository.save(contract);
    }
}

