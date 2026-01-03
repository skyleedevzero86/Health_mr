package com.sleekydz86.finance.contract.service;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.finance.contract.dto.ContractRelayDetailResponse;
import com.sleekydz86.finance.contract.dto.ContractRelayRequest;
import com.sleekydz86.finance.contract.dto.ContractRelayResponse;
import com.sleekydz86.finance.contract.dto.ContractRelayUpdateRequest;
import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import com.sleekydz86.finance.contract.repository.ContractRelayRepository;
import com.sleekydz86.finance.contract.repository.ContractRepository;
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
public class ContractRelayService implements BaseService<ContractRelayEntity, Long> {

    private final ContractRelayRepository contractRelayRepository;
    private final ContractRepository contractRepository;
    private final PatientService patientService;
    private final ContractService contractService;

    public ContractRelayResponse getContractRelayById(Long contractRelayId) {
        ContractRelayEntity relay = validateExists(contractRelayRepository, contractRelayId,
                "연결을 찾을 수 없습니다. ID: " + contractRelayId);
        return ContractRelayResponse.from(relay);
    }

    public ContractRelayDetailResponse getContractRelayDetailById(Long contractRelayId) {
        ContractRelayEntity relay = validateExists(contractRelayRepository, contractRelayId,
                "연결을 찾을 수 없습니다. ID: " + contractRelayId);
        return ContractRelayDetailResponse.from(relay);
    }

    public Page<ContractRelayResponse> getAllContractRelays(Pageable pageable) {
        Page<ContractRelayEntity> relays = contractRelayRepository.findAll(pageable);
        return relays.map(ContractRelayResponse::from);
    }

    public List<ContractRelayResponse> getContractRelaysByPatientNo(Long patientNo) {
        patientService.getPatientByNo(patientNo);
        List<ContractRelayEntity> relays = contractRelayRepository.findByPatientEntity_PatientNo(patientNo);
        return relays.stream().map(ContractRelayResponse::from).collect(Collectors.toList());
    }

    public Page<ContractRelayResponse> getContractRelaysByContractCode(Long contractCode, Pageable pageable) {
        contractService.getContractByCode(contractCode);
        List<ContractRelayEntity> relays = contractRelayRepository.findByContractEntity_ContractCode(contractCode);
        return org.springframework.data.domain.PageImpl.of(
                relays.stream().map(ContractRelayResponse::from).collect(Collectors.toList()),
                pageable,
                relays.size()
        );
    }

    public List<ContractRelayResponse> getActiveContractRelaysByPatientNo(Long patientNo) {
        patientService.getPatientByNo(patientNo);
        List<ContractRelayEntity> relays = contractRelayRepository.findActiveContractRelaysByPatientNo(patientNo);
        return relays.stream().map(ContractRelayResponse::from).collect(Collectors.toList());
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ContractRelayResponse createContractRelay(ContractRelayRequest request) {

        var patient = patientService.getPatientByNo(request.getPatientNo());

        ContractEntity contract = contractRepository.findByContractCode(request.getContractCode())
                .orElseThrow(() -> new NotFoundException("계약처를 찾을 수 없습니다. Code: " + request.getContractCode()));

        if (contractRelayRepository.findByPatientEntity_PatientNoAndContractEntity_ContractCode(
                request.getPatientNo(), request.getContractCode()).isPresent()) {
            throw new DuplicateException("이미 연결된 환자-계약처입니다.");
        }

        ContractRelayEntity relay = ContractRelayEntity.builder()
                .patientEntity(patient)
                .contractEntity(contract)
                .isActive(true)
                .build();

        ContractRelayEntity saved = contractRelayRepository.save(relay);
        return ContractRelayResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ContractRelayResponse updateContractRelay(Long contractRelayId, ContractRelayUpdateRequest request) {
        ContractRelayEntity relay = validateExists(contractRelayRepository, contractRelayId,
                "연결을 찾을 수 없습니다. ID: " + contractRelayId);

        if (request.getIsActive() != null) {
            relay.setIsActive(request.getIsActive());
        }

        if (request.getRelayStartDate() != null) {
            relay.setRelayStartDate(request.getRelayStartDate());
        }

        if (request.getRelayEndDate() != null) {
            relay.setRelayEndDate(request.getRelayEndDate());
        }

        ContractRelayEntity saved = contractRelayRepository.save(relay);
        return ContractRelayResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.DELETE)
    public void deleteContractRelay(Long contractRelayId) {
        ContractRelayEntity relay = validateExists(contractRelayRepository, contractRelayId,
                "연결을 찾을 수 없습니다. ID: " + contractRelayId);

        // 소프트 삭제 (isActive를 false로 변경)
        relay.setIsActive(false);
        contractRelayRepository.save(relay);
    }

    @Transactional
    public void activateContractRelay(Long contractRelayId) {
        ContractRelayEntity relay = validateExists(contractRelayRepository, contractRelayId,
                "연결을 찾을 수 없습니다. ID: " + contractRelayId);
        relay.setIsActive(true);
        contractRelayRepository.save(relay);
    }

    @Transactional
    public void deactivateContractRelay(Long contractRelayId) {
        ContractRelayEntity relay = validateExists(contractRelayRepository, contractRelayId,
                "연결을 찾을 수 없습니다. ID: " + contractRelayId);
        relay.setIsActive(false);
        contractRelayRepository.save(relay);
    }
}

