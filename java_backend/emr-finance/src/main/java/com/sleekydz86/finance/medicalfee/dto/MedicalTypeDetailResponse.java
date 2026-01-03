package com.sleekydz86.finance.medicalfee.dto;

import com.sleekydz86.finance.medicalfee.entity.MedicalTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalTypeDetailResponse {

    private Long medicalTypeId;
    private String medicalTypeCode;
    private String medicalTypeName;
    private Long medicalTypeFee;
    private String medicalTypeDescription;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MedicalTypeDetailResponse from(MedicalTypeEntity entity) {
        MedicalTypeDetailResponse response = new MedicalTypeDetailResponse();
        response.setMedicalTypeId(entity.getMedicalTypeId());
        response.setMedicalTypeCode(entity.getMedicalTypeCode());
        response.setMedicalTypeName(entity.getMedicalTypeName());
        response.setMedicalTypeFee(entity.getMedicalTypeFee());
        response.setMedicalTypeDescription(entity.getMedicalTypeDescription());
        response.setIsActive(entity.getIsActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

