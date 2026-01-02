package com.sleekydz86.emrclinical.prescription.dto;

import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PrescriptionSearchRequest {

    private Long patientNo;
    private Long doctorId;
    private PrescriptionStatus status;
    private PrescriptionType type;
    private LocalDate startDate;
    private LocalDate endDate;
}
