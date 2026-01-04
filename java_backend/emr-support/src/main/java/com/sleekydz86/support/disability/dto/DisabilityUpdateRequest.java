package com.sleekydz86.support.disability.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisabilityUpdateRequest {
    private String disabilityGrade;
    private String disabilityType;
    private String disabilityDeviceYN;
    private String disabilityDeviceType;
}

