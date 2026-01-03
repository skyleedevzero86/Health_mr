package com.sleekydz86.finance.medicalfee.dto;

import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalFeeResponse {

    private Long medicalFeeId;
    private Long medicalTypeId;
    private String medicalTypeName;
    private String medicalTypeCode;
    private Long treatmentId;
    private Long medicalFeeAmount;
    private Integer quantity;

    public static MedicalFeeResponse from(MedicalFeeEntity entity) {
        MedicalFeeResponse response = new MedicalFeeResponse();
        response.setMedicalFeeId(entity.getMedicalFeeId());
        response.setMedicalTypeId(entity.getMedicalTypeEntity().getMedicalTypeId());
        response.setMedicalTypeName(entity.getMedicalTypeEntity().getMedicalTypeName());
        response.setMedicalTypeCode(entity.getMedicalTypeEntity().getMedicalTypeCode());
        response.setTreatmentId(entity.getTreatmentEntity().getTreatmentId());
        response.setMedicalFeeAmount(entity.getMedicalFeeAmount());
        response.setQuantity(entity.getQuantity());
        return response;
    }
}

