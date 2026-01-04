package com.sleekydz86.support.equipment.integration.service;

import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.integration.dto.EquipmentIntegrationRequest;
import com.sleekydz86.support.equipment.integration.dto.EquipmentIntegrationResponse;
import com.sleekydz86.support.equipment.integration.entity.EquipmentIntegrationEntity;
import com.sleekydz86.support.equipment.integration.repository.EquipmentIntegrationRepository;
import com.sleekydz86.support.equipment.integration.type.IntegrationProtocol;
import com.sleekydz86.support.equipment.integration.type.IntegrationStatus;
import com.sleekydz86.support.equipment.integration.type.IntegrationType;
import com.sleekydz86.support.equipment.repository.EquipmentRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentIntegrationService {

    private final EquipmentIntegrationRepository integrationRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public EquipmentIntegrationResponse registerIntegration(EquipmentIntegrationRequest request) {

        EquipmentEntity equipment = equipmentRepository.findByEquipmentId(request.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("장비 정보를 찾을 수 없습니다."));

        integrationRepository.findByEquipmentEntity_EquipmentId(request.getEquipmentId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("이미 연동 정보가 등록되어 있습니다.");
                });

        EquipmentIntegrationEntity integration = EquipmentIntegrationEntity.builder()
                .equipmentEntity(equipment)
                .integrationType(request.getIntegrationType())
                .integrationProtocol(request.getIntegrationProtocol())
                .hisEndpointUrl(request.getHisEndpointUrl())
                .hl7EndpointUrl(request.getHl7EndpointUrl())
                .fhirEndpointUrl(request.getFhirEndpointUrl())
                .apiKey(request.getApiKey())
                .apiSecret(request.getApiSecret())
                .integrationStatus(IntegrationStatus.ACTIVE)
                .integrationConfig(request.getIntegrationConfig())
                .build();

        EquipmentIntegrationEntity saved = integrationRepository.save(integration);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EquipmentIntegrationResponse getIntegration(Long integrationId) {
        EquipmentIntegrationEntity integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("연동 정보를 찾을 수 없습니다."));
        return toResponse(integration);
    }

    @Transactional(readOnly = true)
    public EquipmentIntegrationResponse getIntegrationByEquipmentId(Long equipmentId) {
        EquipmentIntegrationEntity integration = integrationRepository
                .findByEquipmentEntity_EquipmentId(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장비의 연동 정보를 찾을 수 없습니다."));
        return toResponse(integration);
    }

    @Transactional(readOnly = true)
    public List<EquipmentIntegrationResponse> getIntegrationsByType(IntegrationType type) {
        List<EquipmentIntegrationEntity> integrations = integrationRepository.findByIntegrationType(type);
        return integrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentIntegrationResponse> getIntegrationsByProtocol(IntegrationProtocol protocol) {
        List<EquipmentIntegrationEntity> integrations = integrationRepository.findByIntegrationProtocol(protocol);
        return integrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipmentIntegrationResponse updateIntegrationStatus(Long integrationId, IntegrationStatus status) {
        EquipmentIntegrationEntity integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("연동 정보를 찾을 수 없습니다."));

        EquipmentIntegrationEntity updated = EquipmentIntegrationEntity.builder()
                .equipmentEntity(integration.getEquipmentEntity())
                .integrationType(integration.getIntegrationType())
                .integrationProtocol(integration.getIntegrationProtocol())
                .hisEndpointUrl(integration.getHisEndpointUrl())
                .hl7EndpointUrl(integration.getHl7EndpointUrl())
                .fhirEndpointUrl(integration.getFhirEndpointUrl())
                .apiKey(integration.getApiKey())
                .apiSecret(integration.getApiSecret())
                .integrationStatus(status)
                .lastSyncTime(LocalDateTime.now())
                .integrationConfig(integration.getIntegrationConfig())
                .build();

        EquipmentIntegrationEntity saved = integrationRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public void updateLastSyncTime(Long integrationId) {
        EquipmentIntegrationEntity integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("연동 정보를 찾을 수 없습니다."));

        EquipmentIntegrationEntity updated = EquipmentIntegrationEntity.builder()
                .equipmentEntity(integration.getEquipmentEntity())
                .integrationType(integration.getIntegrationType())
                .integrationProtocol(integration.getIntegrationProtocol())
                .hisEndpointUrl(integration.getHisEndpointUrl())
                .hl7EndpointUrl(integration.getHl7EndpointUrl())
                .fhirEndpointUrl(integration.getFhirEndpointUrl())
                .apiKey(integration.getApiKey())
                .apiSecret(integration.getApiSecret())
                .integrationStatus(integration.getIntegrationStatus())
                .lastSyncTime(LocalDateTime.now())
                .integrationConfig(integration.getIntegrationConfig())
                .build();

        integrationRepository.save(updated);
    }

    private EquipmentIntegrationResponse toResponse(EquipmentIntegrationEntity integration) {
        EquipmentEntity equipment = integration.getEquipmentEntity();
        return EquipmentIntegrationResponse.builder()
                .integrationId(integration.getIntegrationId())
                .equipmentId(equipment.getEquipmentId())
                .equipmentName(equipment.getEquipmentName())
                .integrationType(integration.getIntegrationType())
                .integrationProtocol(integration.getIntegrationProtocol())
                .hisEndpointUrl(integration.getHisEndpointUrl())
                .hl7EndpointUrl(integration.getHl7EndpointUrl())
                .fhirEndpointUrl(integration.getFhirEndpointUrl())
                .integrationStatus(integration.getIntegrationStatus())
                .lastSyncTime(integration.getLastSyncTime())
                .createdAt(integration.getCreatedDate())
                .updatedAt(integration.getLastModifiedDate())
                .build();
    }
}