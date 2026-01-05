package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeDetailResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeRequest;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeUpdateRequest;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import com.sleekydz86.finance.medicalfee.entity.MedicalTypeEntity;
import com.sleekydz86.finance.medicalfee.repository.MedicalFeeRepository;
import com.sleekydz86.finance.medicalfee.repository.MedicalTypeRepository;
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
public class MedicalTypeService implements BaseService<MedicalTypeEntity, Long> {

    private final MedicalTypeRepository medicalTypeRepository;
    private final MedicalFeeRepository medicalFeeRepository;
    private final NonCoveredMedicalFeeSyncService nonCoveredMedicalFeeSyncService;

    public MedicalTypeResponse getMedicalTypeById(Long medicalTypeId) {
        MedicalTypeEntity medicalType = validateExists(medicalTypeRepository, medicalTypeId,
                "진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId);
        return MedicalTypeResponse.from(medicalType);
    }

    public MedicalTypeDetailResponse getMedicalTypeDetailById(Long medicalTypeId) {
        MedicalTypeEntity medicalType = validateExists(medicalTypeRepository, medicalTypeId,
                "진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId);
        return MedicalTypeDetailResponse.from(medicalType);
    }

    public MedicalTypeResponse getMedicalTypeByCode(String code) {
        MedicalTypeEntity medicalType = medicalTypeRepository.findByMedicalTypeCode(code)
                .orElseThrow(() -> new NotFoundException("진료 유형을 찾을 수 없습니다. Code: " + code));
        return MedicalTypeResponse.from(medicalType);
    }

    public Page<MedicalTypeResponse> getAllMedicalTypes(Pageable pageable) {
        Page<MedicalTypeEntity> medicalTypes = medicalTypeRepository.findAll(pageable);
        return medicalTypes.map(MedicalTypeResponse::from);
    }

    public List<MedicalTypeResponse> getActiveMedicalTypes() {
        List<MedicalTypeEntity> medicalTypes = medicalTypeRepository.findByIsActive(true);
        return medicalTypes.stream().map(MedicalTypeResponse::from).collect(Collectors.toList());
    }

    public List<MedicalTypeResponse> searchMedicalTypes(String keyword) {
        List<MedicalTypeEntity> medicalTypes = medicalTypeRepository.searchMedicalTypes(keyword);
        return medicalTypes.stream().map(MedicalTypeResponse::from).collect(Collectors.toList());
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public MedicalTypeResponse createMedicalType(MedicalTypeRequest request) {

        validateNotDuplicate(medicalTypeRepository.existsByMedicalTypeCode(request.getMedicalTypeCode()),
                "이미 존재하는 진료 유형 코드입니다: " + request.getMedicalTypeCode());

        validateNotDuplicate(medicalTypeRepository.existsByMedicalTypeName(request.getMedicalTypeName()),
                "이미 존재하는 진료 유형명입니다: " + request.getMedicalTypeName());

        MedicalTypeEntity medicalType = MedicalTypeEntity.builder()
                .medicalTypeCode(request.getMedicalTypeCode())
                .medicalTypeName(request.getMedicalTypeName())
                .medicalTypeFee(Money.of(request.getMedicalTypeFee()))
                .medicalTypeDescription(request.getMedicalTypeDescription())
                .isActive(true)
                .build();

        MedicalTypeEntity saved = medicalTypeRepository.save(medicalType);

        nonCoveredMedicalFeeSyncService.syncMedicalTypeFeeForCurrentInstitution(saved.getMedicalTypeId());

        return MedicalTypeResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public MedicalTypeResponse updateMedicalType(Long medicalTypeId, MedicalTypeUpdateRequest request) {
        MedicalTypeEntity medicalType = validateExists(medicalTypeRepository, medicalTypeId,
                "진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId);

        if (!medicalType.getMedicalTypeCode().equals(request.getMedicalTypeCode())) {
            validateNotDuplicate(medicalTypeRepository.existsByMedicalTypeCode(request.getMedicalTypeCode()),
                    "이미 존재하는 진료 유형 코드입니다: " + request.getMedicalTypeCode());
        }

        if (!medicalType.getMedicalTypeName().equals(request.getMedicalTypeName())) {
            validateNotDuplicate(medicalTypeRepository.existsByMedicalTypeName(request.getMedicalTypeName()),
                    "이미 존재하는 진료 유형명입니다: " + request.getMedicalTypeName());
        }

        if (!medicalType.getMedicalTypeCode().equals(request.getMedicalTypeCode())) {
            medicalType.updateCode(request.getMedicalTypeCode());
        }
        medicalType.updateInfo(request.getMedicalTypeName(), request.getMedicalTypeDescription());
        if (request.getMedicalTypeFee() != null) {
            medicalType.updateFee(Money.of(request.getMedicalTypeFee()));
        }

        MedicalTypeEntity saved = medicalTypeRepository.save(medicalType);
        return MedicalTypeResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.DELETE)
    public void deleteMedicalType(Long medicalTypeId) {
        MedicalTypeEntity medicalType = validateExists(medicalTypeRepository, medicalTypeId,
                "진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId);

        List<MedicalFeeEntity> medicalFees = medicalFeeRepository.findByMedicalTypeEntity_MedicalTypeId(medicalTypeId);
        if (!medicalFees.isEmpty()) {
            throw new BusinessException(
                    "사용 중인 진료 유형은 삭제할 수 없습니다.");
        }

        medicalType.deactivate();
        medicalTypeRepository.save(medicalType);
    }

    @Transactional
    public void activateMedicalType(Long medicalTypeId) {
        MedicalTypeEntity medicalType = validateExists(medicalTypeRepository, medicalTypeId,
                "진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId);
        medicalType.activate();
        medicalTypeRepository.save(medicalType);
    }

    @Transactional
    public void deactivateMedicalType(Long medicalTypeId) {
        MedicalTypeEntity medicalType = validateExists(medicalTypeRepository, medicalTypeId,
                "진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId);
        medicalType.deactivate();
        medicalTypeRepository.save(medicalType);
    }
}
