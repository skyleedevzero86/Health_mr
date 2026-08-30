package com.sleekydz86.emrclinical.diagnosis.dto;

import com.sleekydz86.emrclinical.diagnosis.entity.DiagnosisCertificateEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DiagnosisCertificateResponse {

    private Long certificateId;
    private String certificateNumber;
    private String patientName;
    private String doctorName;
    private String kcdCode;
    private String diagnosisName;
    private LocalDate diagnosisDate;
    private String clinicalFindings;
    private String purpose;
    private LocalDateTime issuedAt;
    private String status;

    public static DiagnosisCertificateResponse from(DiagnosisCertificateEntity entity) {
        return DiagnosisCertificateResponse.builder()
                .certificateId(entity.getCertificateId())
                .certificateNumber(entity.getCertificateNumber())
                .patientName(entity.getPatient().getPatientName())
                .doctorName(entity.getDoctor().getName())
                .kcdCode(entity.getKcdCode().getCode())
                .diagnosisName(entity.getDiagnosisName())
                .diagnosisDate(entity.getDiagnosisDate())
                .clinicalFindings(entity.getClinicalFindings())
                .purpose(entity.getPurpose())
                .issuedAt(entity.getIssuedAt())
                .status(entity.getStatus().name())
                .build();
    }
}
