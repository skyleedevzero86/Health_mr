package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TreatmentFilter {

    private final RegionCodeExtractor regionCodeExtractor;

    public List<TreatmentEntity> filterTreatments(
            List<TreatmentEntity> treatments,
            String institutionType,
            String regionCode) {

        return treatments.stream()
                .filter(t -> {
                    if (institutionType != null) {
                        try {
                            TreatmentType type = TreatmentType.valueOf(institutionType.toUpperCase());
                            if (t.getTreatmentType() != type) {
                                return false;
                            }
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    }
                    if (regionCode != null && t.getPatientEntity() != null) {
                        String patientAddress = t.getPatientEntity().getPatientAddress();
                        if (patientAddress == null) {
                            return false;
                        }
                        String extractedCode = regionCodeExtractor.extractRegionCode(patientAddress);
                        if (extractedCode == null || !extractedCode.equals(regionCode)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
