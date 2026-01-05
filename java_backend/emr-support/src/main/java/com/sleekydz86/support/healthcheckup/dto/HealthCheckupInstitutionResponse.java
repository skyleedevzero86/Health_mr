package com.sleekydz86.support.healthcheckup.dto;

import com.sleekydz86.support.healthcheckup.entity.HealthCheckupInstitutionEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthCheckupInstitutionResponse {
    private Long institutionId;
    private String regionCode;
    private String regionName;
    private String institutionName;
    private String institutionType;
    private String address;
    private String sido;
    private String sigungu;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private Boolean isActive;

    public static HealthCheckupInstitutionResponse from(HealthCheckupInstitutionEntity entity) {
        return HealthCheckupInstitutionResponse.builder()
                .institutionId(entity.getInstitutionId())
                .regionCode(entity.getRegionCode())
                .regionName(entity.getRegionName())
                .institutionName(entity.getInstitutionName())
                .institutionType(entity.getInstitutionType())
                .address(entity.getAddress())
                .sido(entity.getSido())
                .sigungu(entity.getSigungu())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .phoneNumber(entity.getPhoneNumber())
                .isActive(entity.isActive())
                .build();
    }
}

