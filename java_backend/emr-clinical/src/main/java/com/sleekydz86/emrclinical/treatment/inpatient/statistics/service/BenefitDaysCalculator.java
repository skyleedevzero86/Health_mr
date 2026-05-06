package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionItemEntity;
import com.sleekydz86.emrclinical.prescription.repository.PrescriptionRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BenefitDaysCalculator {

    private final PrescriptionRepository prescriptionRepository;

    public long calculateBenefitDays(
            List<TreatmentEntity> treatments,
            LocalDate startDate,
            LocalDate endDate) {

        Set<LocalDate> visitDates = treatments.stream()
                .map(t -> t.getTreatmentDate().toLocalDate())
                .collect(java.util.stream.Collectors.toSet());

        Set<LocalDate> prescriptionDates = new HashSet<>();
        for (TreatmentEntity treatment : treatments) {
            prescriptionRepository.findByTreatmentEntity_TreatmentId(treatment.getTreatmentId())
                    .ifPresent(prescription -> {
                        LocalDate prescriptionDate = prescription.getPrescriptionDate().toLocalDate();
                        if (prescription.getPrescriptionItems() != null) {
                            for (PrescriptionItemEntity item : prescription.getPrescriptionItems()) {
                                for (int i = 0; i < item.getDays(); i++) {
                                    LocalDate date = prescriptionDate.plusDays(i);
                                    if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                                        prescriptionDates.add(date);
                                    }
                                }
                            }
                        }
                    });
        }

        Set<LocalDate> allDates = new HashSet<>(visitDates);
        allDates.addAll(prescriptionDates);

        return allDates.size();
    }
}

