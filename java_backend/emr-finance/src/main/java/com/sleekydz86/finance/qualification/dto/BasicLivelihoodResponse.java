package com.sleekydz86.finance.qualification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasicLivelihoodResponse {

    private Boolean eligible;
    private String type; // 생계급여, 주거급여, 의료급여, 교육급여
    private String recipientNumber;
}
