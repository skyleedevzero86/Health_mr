package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import org.springframework.stereotype.Component;

@Component
public class RegionCodeExtractor {

    public String extractRegionCode(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        if (address.startsWith("서울")) return "11";
        if (address.startsWith("부산")) return "26";
        if (address.startsWith("대구")) return "27";
        if (address.startsWith("인천")) return "28";
        if (address.startsWith("광주")) return "29";
        if (address.startsWith("대전")) return "30";
        if (address.startsWith("울산")) return "31";
        if (address.startsWith("세종")) return "36";
        if (address.startsWith("경기")) return "41";
        if (address.startsWith("강원")) return "42";
        if (address.startsWith("충북")) return "43";
        if (address.startsWith("충남")) return "44";
        if (address.startsWith("전북")) return "45";
        if (address.startsWith("전남")) return "46";
        if (address.startsWith("경북")) return "47";
        if (address.startsWith("경남")) return "48";
        if (address.startsWith("제주")) return "50";
        return null;
    }
}

