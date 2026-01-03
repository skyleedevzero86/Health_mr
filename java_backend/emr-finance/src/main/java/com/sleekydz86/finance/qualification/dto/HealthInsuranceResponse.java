package com.sleekydz86.finance.qualification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInsuranceResponse {

    private Boolean eligible;
    private String type;
    private String insuranceNumber;
    private String insuranceCompany;
}
