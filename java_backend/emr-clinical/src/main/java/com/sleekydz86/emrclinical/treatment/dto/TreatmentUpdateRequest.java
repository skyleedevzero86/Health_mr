package com.sleekydz86.emrclinical.treatment.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TreatmentUpdateRequest {

    @Size(max = 2000, message = "진료 코멘트는 최대 2000자까지 가능합니다.")
    private String comment;
    private Long departmentId;
    private Long doctorId;
}
