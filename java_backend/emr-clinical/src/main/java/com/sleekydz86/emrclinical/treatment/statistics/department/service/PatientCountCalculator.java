package com.sleekydz86.emrclinical.treatment.statistics.department.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientCountCalculator {

    public long calculatePatientCount(List<TreatmentEntity> treatments) {
        return treatments.stream()
                .filter(t -> t.getPatientEntity() != null)
                .map(t -> t.getPatientEntity().getPatientNo())
                .distinct()
                .count();
    }
}

