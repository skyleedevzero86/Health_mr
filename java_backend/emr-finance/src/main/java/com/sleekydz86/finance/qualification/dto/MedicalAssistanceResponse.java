package com.sleekydz86.finance.qualification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalAssistanceResponse {

    private Boolean eligible;
    private String type; // 1종, 2종
    private String assistanceNumber;
}

