package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VisitDaysCalculator {

    public long calculateVisitDays(List<TreatmentEntity> treatments) {
        return treatments.stream()
                .map(t -> t.getTreatmentDate().toLocalDate())
                .distinct()
                .count();
    }
}

