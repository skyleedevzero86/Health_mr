package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicalFeeCalculator {

    private static final long DEFAULT_TREATMENT_BASE_FEE = 15000L;
    private static final long DEFAULT_BENEFIT_RATIO_PERCENT = 70L;

    public long calculateMedicalFee(List<TreatmentEntity> treatments) {
        if (treatments == null || treatments.isEmpty()) {
            return 0L;
        }
        return treatments.size() * DEFAULT_TREATMENT_BASE_FEE;
    }

    public long calculateBenefitFee(List<TreatmentEntity> treatments) {
        long totalFee = calculateMedicalFee(treatments);
        return (totalFee * DEFAULT_BENEFIT_RATIO_PERCENT) / 100L;
    }
}
