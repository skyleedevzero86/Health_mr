package com.sleekydz86.finance.medicalfee.dto;

import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalFeeDetailResponse {

    private Long medicalFeeId;
    private Long medicalTypeId;
    private String medicalTypeName;
    private String medicalTypeCode;
    private Long treatmentId;
    private Long medicalFeeAmount;
    private Integer quantity;
    private Long totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MedicalFeeDetailResponse from(MedicalFeeEntity entity) {
        MedicalFeeDetailResponse response = new MedicalFeeDetailResponse();
        response.setMedicalFeeId(entity.getMedicalFeeId());
        response.setMedicalTypeId(entity.getMedicalTypeEntity().getMedicalTypeId());
        response.setMedicalTypeName(entity.getMedicalTypeEntity().getMedicalTypeName());
        response.setMedicalTypeCode(entity.getMedicalTypeEntity().getMedicalTypeCode());
        response.setTreatmentId(entity.getTreatmentEntity().getTreatmentId());
        response.setMedicalFeeAmount(entity.getMedicalFeeAmount());
        response.setQuantity(entity.getQuantity());
        response.setTotalAmount(entity.getMedicalFeeAmount() * entity.getQuantity());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

