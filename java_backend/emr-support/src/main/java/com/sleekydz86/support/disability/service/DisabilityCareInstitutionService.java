package com.sleekydz86.support.disability.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.support.disability.dto.CareInstitutionRecommendationResponse;
import com.sleekydz86.support.disability.dto.CareInstitutionResponse;
import com.sleekydz86.support.disability.dto.CareInstitutionSearchRequest;
import com.sleekydz86.support.disability.entity.DisabilityCareInstitutionEntity;
import com.sleekydz86.support.disability.repository.DisabilityCareInstitutionRepository;
import com.sleekydz86.support.disability.repository.DisabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisabilityCareInstitutionService {

    private final DisabilityCareInstitutionRepository institutionRepository;
    private final DisabilityRepository disabilityRepository;
    private final PatientService patientService;

    public List<CareInstitutionRecommendationResponse> recommendInstitutions(Long patientNo) {
        var disabilityEntityOptional = disabilityRepository.findByPatientEntity_PatientNo(patientNo);
        if (disabilityEntityOptional.isEmpty()) {
            return Collections.emptyList();
        }
        
        var disabilityEntity = disabilityEntityOptional.get();
        var patient = disabilityEntity.getPatientEntity();
        String patientAddress = patient.getPatientAddress();
        
        String disabilityType = disabilityEntity.getDisabilityType();
        String disabilityGrade = disabilityEntity.getDisabilityGrade();

        List<DisabilityCareInstitutionEntity> institutions = findSuitableInstitutions(
            disabilityType,
            disabilityGrade,
            patientAddress
        );

        return institutions.stream()
            .map(institution -> buildRecommendation(institution, patientAddress))
            .sorted(Comparator.comparing(CareInstitutionRecommendationResponse::getDistance))
            .limit(10)
            .collect(Collectors.toList());
    }

    private List<DisabilityCareInstitutionEntity> findSuitableInstitutions(
            String disabilityType,
            String disabilityGrade,
            String patientAddress) {

        List<DisabilityCareInstitutionEntity> primaryCare =
            institutionRepository.findByServiceTypeAndIsActive("주장애관리", true);

        List<DisabilityCareInstitutionEntity> integratedCare =
            institutionRepository.findByServiceTypeAndIsActive("통합관리", true);

        String sido = extractSido(patientAddress);
        List<DisabilityCareInstitutionEntity> filtered = Stream
            .concat(primaryCare.stream(), integratedCare.stream())
            .filter(inst -> inst.getSido() != null && inst.getSido().contains(sido))
            .collect(Collectors.toList());

        return filtered.isEmpty() ?
            Stream.concat(primaryCare.stream(), integratedCare.stream())
                .collect(Collectors.toList()) : filtered;
    }

    private CareInstitutionRecommendationResponse buildRecommendation(
            DisabilityCareInstitutionEntity institution,
            String patientAddress) {

        double distance = calculateDistance(patientAddress, institution.getAddress());
        double recommendationScore = calculateRecommendationScore(institution, distance);

        return CareInstitutionRecommendationResponse.builder()
            .institutionId(institution.getInstitutionId())
            .institutionName(institution.getInstitutionName())
            .institutionType(institution.getInstitutionType())
            .serviceType(institution.getServiceType())
            .address(institution.getAddress())
            .distance(distance)
            .recommendationScore(recommendationScore)
            .build();
    }

    private double calculateRecommendationScore(
            DisabilityCareInstitutionEntity institution,
            double distance) {

        double score = 100.0;

        if ("주장애관리".equals(institution.getServiceType())) {
            score += 30;
        } else if ("통합관리".equals(institution.getServiceType())) {
            score += 20;
        } else {
            score += 10;
        }

        if (distance < 5) {
            score += 20;
        } else if (distance < 10) {
            score += 10;
        } else if (distance < 20) {
            score += 5;
        } else {
            score -= 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    private double calculateDistance(String address1, String address2) {
        String sido1 = extractSido(address1);
        String sido2 = extractSido(address2);
        
        if (sido1 != null && sido2 != null && sido1.equals(sido2)) {
            String sigungu1 = extractSigungu(address1);
            String sigungu2 = extractSigungu(address2);
            
            if (sigungu1 != null && sigungu2 != null && sigungu1.equals(sigungu2)) {
                return 5.0;
            }
            return 15.0;
        }
        return 50.0;
    }

    private String extractSido(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        
        String[] sidoKeywords = {"서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", 
                                  "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원도", 
                                  "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"};
        
        for (String sido : sidoKeywords) {
            if (address.contains(sido)) {
                return sido;
            }
        }
        
        if (address.length() > 2) {
            return address.substring(0, 2);
        }
        return null;
    }

    private String extractSigungu(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        
        String sido = extractSido(address);
        if (sido == null) {
            return null;
        }
        
        int sidoIndex = address.indexOf(sido);
        if (sidoIndex == -1) {
            return null;
        }
        
        String afterSido = address.substring(sidoIndex + sido.length());
        String[] parts = afterSido.split("\\s+");
        
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return null;
    }

    public Page<CareInstitutionResponse> searchInstitutions(CareInstitutionSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        Page<DisabilityCareInstitutionEntity> entities = institutionRepository.searchInstitutions(
            request.getServiceType(),
            request.getSido(),
            request.getInstitutionType(),
            pageable
        );
        
        return entities.map(CareInstitutionResponse::from);
    }
}

