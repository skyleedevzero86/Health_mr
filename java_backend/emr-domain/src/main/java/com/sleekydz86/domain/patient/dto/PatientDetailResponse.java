package com.sleekydz86.domain.patient.dto;

import com.sleekydz86.core.security.masking.annotation.Sensitive;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record PatientDetailResponse(
        Long patientNo,
        String patientName,
        @Sensitive(type = Sensitive.MaskingType.RRN)
        String patientRrn,
        String patientGender,
        LocalDate patientBirth,
        String patientAddress,
        @Sensitive(type = Sensitive.MaskingType.EMAIL)
        String patientEmail,
        @Sensitive(type = Sensitive.MaskingType.PHONE)
        String patientTel,
        String patientForeign,
        String patientPassport,
        String patientHypassYN,
        LocalDate patientLastVisit,
        String guardian,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
    public static PatientDetailResponse from(PatientEntity entity) {
        return PatientDetailResponse.builder()
                .patientNo(entity.getPatientNo())
                .patientName(entity.getPatientName())
                .patientRrn(entity.getPatientRrn())
                .patientGender(entity.getPatientGender())
                .patientBirth(entity.getPatientBirth())
                .patientAddress(entity.getPatientAddress())
                .patientEmail(entity.getPatientEmail())
                .patientTel(entity.getPatientTel())
                .patientForeign(entity.getPatientForeign())
                .patientPassport(entity.getPatientPassport())
                .patientHypassYN(entity.getPatientHypassYN())
                .patientLastVisit(entity.getPatientLastVisit())
                .guardian(entity.getGuardian())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}

