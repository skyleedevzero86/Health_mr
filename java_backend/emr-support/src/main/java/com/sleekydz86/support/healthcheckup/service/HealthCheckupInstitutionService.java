package com.sleekydz86.support.healthcheckup.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.support.healthcheckup.dto.HealthCheckupInstitutionRecommendationResponse;
import com.sleekydz86.support.healthcheckup.dto.HealthCheckupInstitutionResponse;
import com.sleekydz86.support.healthcheckup.dto.HealthCheckupInstitutionSearchRequest;
import com.sleekydz86.support.healthcheckup.entity.HealthCheckupInstitutionEntity;
import com.sleekydz86.support.healthcheckup.repository.HealthCheckupInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthCheckupInstitutionService {

    private final HealthCheckupInstitutionRepository institutionRepository;
    private final PatientService patientService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "health-checkup:institution:";
    private static final long CACHE_TTL = 86400;

    public Page<HealthCheckupInstitutionResponse> searchInstitutions(
            HealthCheckupInstitutionSearchRequest request) {

        String cacheKey = buildCacheKey(request);
        
        @SuppressWarnings("unchecked")
        Page<HealthCheckupInstitutionResponse> cached = 
                (Page<HealthCheckupInstitutionResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<HealthCheckupInstitutionEntity> institutions =
            institutionRepository.searchInstitutions(
                request.getRegionCode(),
                request.getInstitutionType(),
                request.getInstitutionName(),
                request.getSido(),
                pageable
            );

        Page<HealthCheckupInstitutionResponse> result = institutions.map(HealthCheckupInstitutionResponse::from);
        
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
        
        return result;
    }

    public List<HealthCheckupInstitutionRecommendationResponse> recommendInstitutions(
            Long patientNo) {

        PatientEntity patient = patientService.getPatientByNo(patientNo);
        String patientAddress = patient.getPatientAddress();

        if (patientAddress == null || patientAddress.isBlank()) {
            return List.of();
        }

        String sido = extractSido(patientAddress);
        String regionCode = extractRegionCode(patientAddress);

        List<HealthCheckupInstitutionEntity> institutions;

        if (regionCode != null) {
            institutions = institutionRepository.findByRegionCodeAndIsActive(regionCode, true);
        } else if (sido != null && !sido.isBlank()) {
            institutions = institutionRepository.findBySidoAndIsActive(sido, true);
        } else {
            institutions = institutionRepository.findAllActive(PageRequest.of(0, 100)).getContent();
        }

        return institutions.stream()
            .map(institution -> buildRecommendation(institution, patientAddress))
            .sorted(Comparator.comparing(HealthCheckupInstitutionRecommendationResponse::getDistance))
            .limit(10)
            .collect(Collectors.toList());
    }

    public HealthCheckupInstitutionResponse getInstitutionById(Long institutionId) {
        HealthCheckupInstitutionEntity institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() -> new NotFoundException("검진기관을 찾을 수 없습니다."));

        return HealthCheckupInstitutionResponse.from(institution);
    }

    public List<HealthCheckupInstitutionResponse> getInstitutionsByRegion(String regionCode) {
        List<HealthCheckupInstitutionEntity> institutions =
            institutionRepository.findByRegionCodeAndIsActive(regionCode, true);

        return institutions.stream()
            .map(HealthCheckupInstitutionResponse::from)
            .collect(Collectors.toList());
    }

    public List<HealthCheckupInstitutionResponse> getInstitutionsByType(String institutionType) {
        List<HealthCheckupInstitutionEntity> institutions =
            institutionRepository.findByInstitutionTypeAndIsActive(institutionType, true);

        return institutions.stream()
            .map(HealthCheckupInstitutionResponse::from)
            .collect(Collectors.toList());
    }

    private HealthCheckupInstitutionRecommendationResponse buildRecommendation(
            HealthCheckupInstitutionEntity institution,
            String patientAddress) {

        double distance = calculateDistance(patientAddress, institution.getAddress());
        double recommendationScore = calculateRecommendationScore(institution, distance);

        return HealthCheckupInstitutionRecommendationResponse.builder()
            .institutionId(institution.getInstitutionId())
            .institutionName(institution.getInstitutionName())
            .institutionType(institution.getInstitutionType())
            .address(institution.getAddress())
            .sido(institution.getSido())
            .sigungu(institution.getSigungu())
            .latitude(institution.getLatitude())
            .longitude(institution.getLongitude())
            .phoneNumber(institution.getPhoneNumber())
            .distance(distance)
            .recommendationScore(recommendationScore)
            .build();
    }

    private double calculateDistance(String address1, String address2) {
        if (address1 == null || address2 == null) {
            return Double.MAX_VALUE;
        }

        if (address1.equals(address2)) {
            return 0.0;
        }

        String sido1 = extractSido(address1);
        String sido2 = extractSido(address2);

        if (sido1 == null || sido2 == null || !sido1.equals(sido2)) {
            return 100.0;
        }

        String sigungu1 = extractSigungu(address1);
        String sigungu2 = extractSigungu(address2);

        if (sigungu1 != null && sigungu2 != null && sigungu1.equals(sigungu2)) {
            return 5.0;
        }

        return 10.0;
    }

    private double calculateRecommendationScore(
            HealthCheckupInstitutionEntity institution,
            double distance) {

        double score = 100.0;

        if ("병원".equals(institution.getInstitutionType()) ||
            "종합병원".equals(institution.getInstitutionType()) ||
            "상급종합병원".equals(institution.getInstitutionType())) {
            score += 30;
        } else if ("의원".equals(institution.getInstitutionType())) {
            score += 20;
        } else if ("보건소".equals(institution.getInstitutionType())) {
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

    private String extractSido(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        if (address.startsWith("서울")) return "서울";
        if (address.startsWith("부산")) return "부산";
        if (address.startsWith("대구")) return "대구";
        if (address.startsWith("인천")) return "인천";
        if (address.startsWith("광주")) return "광주";
        if (address.startsWith("대전")) return "대전";
        if (address.startsWith("울산")) return "울산";
        if (address.startsWith("세종")) return "세종";
        if (address.startsWith("경기")) return "경기";
        if (address.startsWith("강원")) return "강원";
        if (address.startsWith("충북")) return "충북";
        if (address.startsWith("충남")) return "충남";
        if (address.startsWith("전북")) return "전북";
        if (address.startsWith("전남")) return "전남";
        if (address.startsWith("경북")) return "경북";
        if (address.startsWith("경남")) return "경남";
        if (address.startsWith("제주")) return "제주";
        return "";
    }

    private String extractRegionCode(String address) {
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

    private String extractSigungu(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }

        String[] parts = address.split(" ");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    private String buildCacheKey(HealthCheckupInstitutionSearchRequest request) {
        return CACHE_PREFIX + 
                (request.getRegionCode() != null ? request.getRegionCode() : "") + ":" +
                (request.getInstitutionType() != null ? request.getInstitutionType() : "") + ":" +
                (request.getInstitutionName() != null ? request.getInstitutionName() : "") + ":" +
                (request.getSido() != null ? request.getSido() : "") + ":" +
                request.getPage() + ":" +
                request.getSize();
    }
}

