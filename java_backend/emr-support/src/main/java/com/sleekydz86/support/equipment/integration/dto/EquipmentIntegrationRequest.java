package com.sleekydz86.support.equipment.integration.dto;

import com.sleekydz86.support.equipment.integration.type.IntegrationProtocol;
import com.sleekydz86.support.equipment.integration.type.IntegrationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentIntegrationRequest {

    @NotNull(message = "장비 ID는 필수 값입니다.")
    private Long equipmentId;

    @NotNull(message = "연동 타입은 필수 값입니다.")
    private IntegrationType integrationType;

    @NotNull(message = "연동 프로토콜은 필수 값입니다.")
    private IntegrationProtocol integrationProtocol;

    private String hisEndpointUrl;

    private String hl7EndpointUrl;

    private String fhirEndpointUrl;

    private String apiKey;

    private String apiSecret;

    private String integrationConfig;
}