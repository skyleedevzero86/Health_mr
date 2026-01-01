package com.sleekydz86.domain.patient.dto;

import lombok.Getter;

@Getter
public class PatientSearchRequest {
    private String name;
    private String tel;
    private String email;
}
