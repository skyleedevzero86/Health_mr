package com.sleekydz86.support.examination.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationRegisterRequest {

    private Long equipmentId;

    private String equipmentName;

    @NotBlank(message = "검사명은 필수 값입니다.")
    private String examinationName;

    @NotBlank(message = "검사 유형은 필수 값입니다.")
    private String examinationType;

    private String examinationConstraints;

    @NotBlank(message = "검사 장소는 필수 값입니다.")
    private String examinationLocation;

    @NotBlank(message = "검사 가격은 필수 값입니다.")
    private String examinationPrice;
}

