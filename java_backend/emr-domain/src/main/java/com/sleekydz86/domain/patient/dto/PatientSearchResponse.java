package com.sleekydz86.domain.patient.dto;

import com.sleekydz86.core.security.masking.annotation.Sensitive;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import lombok.Builder;
import java.time.LocalDate;

@Builder
public record PatientSearchResponse(
        Long patientNo,
        String patientName,
        @Sensitive(type = Sensitive.MaskingType.PHONE)
        String patientTel,
        @Sensitive(type = Sensitive.MaskingType.EMAIL)
        String patientEmail,
        LocalDate patientLastVisit
) {
    public static PatientSearchResponse from(PatientEntity entity) {
        return PatientSearchResponse.builder()
                .patientNo(entity.getPatientNo())
                .patientName(entity.getPatientName())
                .patientTel(entity.getPatientTel())
                .patientEmail(entity.getPatientEmail())
                .patientLastVisit(entity.getPatientLastVisit())
                .build();
    }
}

