package com.sleekydz86.emrclinical.treatment.dto;

import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class TreatmentSearchRequest {

    private Long patientNo;
    private Long doctorId;
    private Long departmentId;
    private TreatmentType treatmentType;
    private TreatmentStatus treatmentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
}