package com.sleekydz86.emrclinical.treatment.dto;

import com.sleekydz86.emrclinical.types.TreatmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TreatmentCreateRequest {

    @NotNull(message = "진료 유형은 필수입니다.")
    private TreatmentType treatmentType;

    @NotNull(message = "진료 의사는 필수입니다.")
    private Long doctorId;
    private Long checkInId;
    private Long patientNo;
    private Long departmentId;
}
