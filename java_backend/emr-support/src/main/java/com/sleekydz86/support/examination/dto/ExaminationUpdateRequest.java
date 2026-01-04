package com.sleekydz86.support.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationUpdateRequest {

    private Long equipmentId;

    private String examinationName;

    private String examinationType;

    private String examinationConstraints;

    private String examinationLocation;

    private String examinationPrice;
}

