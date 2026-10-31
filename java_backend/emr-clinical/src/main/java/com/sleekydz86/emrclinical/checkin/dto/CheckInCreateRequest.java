package com.sleekydz86.emrclinical.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInCreateRequest {

    @NotNull(message = "환자 번호는 필수입니다.")
    private Long patientNo;

    @NotNull(message = "진료과는 필수입니다.")
    private Long departmentId;

    private Long userId;
}
