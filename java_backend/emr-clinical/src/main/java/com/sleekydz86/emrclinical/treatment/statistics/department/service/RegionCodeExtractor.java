package com.sleekydz86.emrclinical.treatment.statistics.department.service;

import com.sleekydz86.emrclinical.treatment.inpatient.statistics.service.RegionCodeExtractor as InpatientRegionCodeExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionCodeExtractor {

    private final com.sleekydz86.emrclinical.treatment.inpatient.statistics.service.RegionCodeExtractor inpatientRegionCodeExtractor;

    public String extractRegionCode(String address) {
        return inpatientRegionCodeExtractor.extractRegionCode(address);
    }
}

