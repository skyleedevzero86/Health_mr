package com.sleekydz86.domain.institution.service;

import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.institution.dto.InstitutionCreateRequest;
import com.sleekydz86.domain.institution.dto.InstitutionResponse;
import com.sleekydz86.domain.institution.dto.InstitutionUpdateRequest;
import com.sleekydz86.domain.institution.entity.InstitutionEntity;
import com.sleekydz86.domain.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    @Transactional
    public InstitutionResponse create(InstitutionCreateRequest request) {

        if (institutionRepository.existsByInstitutionCode(request.getInstitutionCode())) {
            throw new DuplicateException("이미 사용 중인 기관 코드입니다.");
        }

        InstitutionEntity institution = InstitutionEntity.builder()
                .institutionCode(request.getInstitutionCode())
                .institutionName(request.getInstitutionName())
                .institutionEngName(request.getInstitutionEngName())
                .institutionAddress(request.getInstitutionAddress())
                .institutionTel(request.getInstitutionTel())
                .institutionEmail(request.getInstitutionEmail())
                .directorName(request.getDirectorName())
                .isActive(true)
                .build();

        InstitutionEntity saved = institutionRepository.save(institution);
        return InstitutionResponse.from(saved);
    }

    public InstitutionResponse findByCode(String institutionCode) {
        InstitutionEntity institution = institutionRepository.findActiveByInstitutionCode(institutionCode)
                .orElseThrow(() -> new NotFoundException("기관을 찾을 수 없습니다."));


        if (!TenantContext.isAdmin() && !TenantContext.belongsToTenant(institutionCode)) {
            throw new NotFoundException("기관을 찾을 수 없습니다.");
        }

        return InstitutionResponse.from(institution);
    }

    public InstitutionResponse findById(Long institutionId) {
        InstitutionEntity institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("기관을 찾을 수 없습니다."));

        if (!TenantContext.isAdmin() && !TenantContext.belongsToTenant(institution.getInstitutionCode())) {
            throw new NotFoundException("기관을 찾을 수 없습니다.");
        }

        return InstitutionResponse.from(institution);
    }

    public List<InstitutionResponse> findAll() {
        List<InstitutionEntity> institutions;

        if (TenantContext.isAdmin()) {

            institutions = institutionRepository.findAll();
        } else {

            List<String> tenantIds = TenantContext.getTenantIds();
            if (tenantIds == null || tenantIds.isEmpty()) {
                return List.of();
            }
            institutions = institutionRepository.findAll().stream()
                    .filter(institution -> tenantIds.contains(institution.getInstitutionCode()))
                    .toList();
        }

        return institutions.stream()
                .map(InstitutionResponse::from)
                .toList();
    }

    public List<InstitutionResponse> findAllActive() {
        List<InstitutionEntity> institutions;

        if (TenantContext.isAdmin()) {

            institutions = institutionRepository.findAll().stream()
                    .filter(InstitutionEntity::isActive)
                    .toList();
        } else {

            List<String> tenantIds = TenantContext.getTenantIds();
            if (tenantIds == null || tenantIds.isEmpty()) {
                return List.of();
            }
            institutions = institutionRepository.findAll().stream()
                    .filter(InstitutionEntity::isActive)
                    .filter(institution -> tenantIds.contains(institution.getInstitutionCode()))
                    .toList();
        }

        return institutions.stream()
                .map(InstitutionResponse::from)
                .toList();
    }

    @Transactional
    public InstitutionResponse update(Long institutionId, InstitutionUpdateRequest request) {
        InstitutionEntity institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("기관을 찾을 수 없습니다."));

        institution.update(
                request.getInstitutionName(),
                request.getInstitutionEngName(),
                request.getInstitutionAddress(),
                request.getInstitutionTel(),
                request.getInstitutionEmail(),
                request.getDirectorName()
        );

        InstitutionEntity saved = institutionRepository.save(institution);
        return InstitutionResponse.from(saved);
    }

    @Transactional
    public InstitutionResponse activate(Long institutionId) {
        InstitutionEntity institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("기관을 찾을 수 없습니다."));

        institution.activate();
        InstitutionEntity saved = institutionRepository.save(institution);
        return InstitutionResponse.from(saved);
    }

    @Transactional
    public InstitutionResponse deactivate(Long institutionId) {
        InstitutionEntity institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("기관을 찾을 수 없습니다."));

        institution.deactivate();
        InstitutionEntity saved = institutionRepository.save(institution);
        return InstitutionResponse.from(saved);
    }

    @Transactional
    public void delete(Long institutionId) {
        InstitutionEntity institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("기관을 찾을 수 없습니다."));

        institutionRepository.delete(institution);
    }


    public boolean existsByCode(String institutionCode) {
        return institutionRepository.existsByInstitutionCode(institutionCode);
    }

    public boolean existsActiveByCode(String institutionCode) {
        return institutionRepository.findActiveByInstitutionCode(institutionCode).isPresent();
    }
}

