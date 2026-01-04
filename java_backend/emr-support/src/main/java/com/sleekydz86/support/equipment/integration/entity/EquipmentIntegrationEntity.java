package com.sleekydz86.support.equipment.integration.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.integration.type.IntegrationProtocol;
import com.sleekydz86.support.equipment.integration.type.IntegrationStatus;
import com.sleekydz86.support.equipment.integration.type.IntegrationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "Equipment_Integration")
@Table(name = "equipment_integration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentIntegrationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "integration_id", nullable = false)
    private Long integrationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", referencedColumnName = "equipment_id", nullable = false, unique = true)
    private EquipmentEntity equipmentEntity;

    @Column(name = "integration_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IntegrationType integrationType;

    @Column(name = "integration_protocol", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IntegrationProtocol integrationProtocol;

    @Column(name = "his_endpoint_url", length = 500)
    private String hisEndpointUrl;

    @Column(name = "hl7_endpoint_url", length = 500)
    private String hl7EndpointUrl;

    @Column(name = "fhir_endpoint_url", length = 500)
    private String fhirEndpointUrl;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "api_secret", length = 500)
    private String apiSecret;

    @Column(name = "integration_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IntegrationStatus integrationStatus = IntegrationStatus.ACTIVE;

    @Column(name = "last_sync_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    @Column(name = "integration_config", columnDefinition = "TEXT")
    private String integrationConfig;

    @Builder
    private EquipmentIntegrationEntity(
            EquipmentEntity equipmentEntity,
            IntegrationType integrationType,
            IntegrationProtocol integrationProtocol,
            String hisEndpointUrl,
            String hl7EndpointUrl,
            String fhirEndpointUrl,
            String apiKey,
            String apiSecret,
            IntegrationStatus integrationStatus,
            LocalDateTime lastSyncTime,
            String integrationConfig) {
        this.equipmentEntity = equipmentEntity;
        this.integrationType = integrationType;
        this.integrationProtocol = integrationProtocol;
        this.hisEndpointUrl = hisEndpointUrl;
        this.hl7EndpointUrl = hl7EndpointUrl;
        this.fhirEndpointUrl = fhirEndpointUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.integrationStatus = integrationStatus != null ? integrationStatus : IntegrationStatus.ACTIVE;
        this.lastSyncTime = lastSyncTime;
        this.integrationConfig = integrationConfig;
    }
}

