package com.sleekydz86.emrclinical.treatment.statistics.department.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DepartmentTreatmentFilter {

    public final RegionCodeExtractor regionCodeExtractor;

    public List<TreatmentEntity> filterTreatments(
            List<TreatmentEntity> treatments,
            String departmentName,
            String regionCode) {

        return treatments.stream()
                .filter(t -> {
                    if (departmentName != null && t.getDepartmentEntity() != null) {
                        if (!t.getDepartmentEntity().getName().equals(departmentName)) {
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

