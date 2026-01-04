package com.sleekydz86.support.equipment.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.equipment.integration.type.IntegrationProtocol;
import com.sleekydz86.support.equipment.integration.type.IntegrationStatus;
import com.sleekydz86.support.equipment.integration.type.IntegrationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentIntegrationResponse {

    private Long integrationId;
    private Long equipmentId;
    private String equipmentName;
    private IntegrationType integrationType;
    private IntegrationProtocol integrationProtocol;
    private String hisEndpointUrl;
    private String hl7EndpointUrl;
    private String fhirEndpointUrl;
    private IntegrationStatus integrationStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

