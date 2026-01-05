package com.sleekydz86.support.disability.dto;

import com.sleekydz86.support.disability.entity.DisabilityCareInstitutionEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CareInstitutionResponse {
    private Long institutionId;
    private String institutionType;
    private String institutionName;
    private String serviceType;
    private String address;
    private String sido;
    private String sigungu;
    private Double latitude;
    private Double longitude;
    
    public static CareInstitutionResponse from(DisabilityCareInstitutionEntity entity) {
        return CareInstitutionResponse.builder()
                .institutionId(entity.getInstitutionId())
                .institutionType(entity.getInstitutionType())
                .institutionName(entity.getInstitutionName())
                .serviceType(entity.getServiceType())
                .address(entity.getAddress())
                .sido(entity.getSido())
                .sigungu(entity.getSigungu())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}

