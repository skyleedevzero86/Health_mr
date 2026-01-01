package com.sleekydz86.domain.patient.dto;

import com.sleekydz86.core.security.masking.annotation.Sensitive;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PatientListResponse(
        Long patientNo,
        String patientName,
        @Sensitive(type = Sensitive.MaskingType.PHONE)
        String patientTel,
        @Sensitive(type = Sensitive.MaskingType.EMAIL)
        String patientEmail,
        LocalDate patientLastVisit
) {
    public static PatientListResponse from(PatientEntity entity) {
        return PatientListResponse.builder()
                .patientNo(entity.getPatientNoValue())
                .patientName(entity.getPatientName())
                .patientTel(entity.getPatientTelValue())
                .patientEmail(entity.getPatientEmailValue())
                .patientLastVisit(entity.getPatientLastVisit())
                .build();
    }
}