package com.sleekydz86.finance.qualification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllQualificationsResponse {

    private HealthInsuranceResponse healthInsurance;
    private MedicalAssistanceResponse medicalAssistance;
    private BasicLivelihoodResponse basicLivelihood;
}

