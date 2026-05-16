package com.sleekydz86.emrclinical.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiagnosisCertificateCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotBlank
    private String kcdCode;

    @NotBlank
    private String diagnosisName;

    @NotNull
    private LocalDate diagnosisDate;

    private String clinicalFindings;

    private String purpose;
}
