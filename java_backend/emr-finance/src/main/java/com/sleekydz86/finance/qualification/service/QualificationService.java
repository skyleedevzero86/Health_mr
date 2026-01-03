package com.sleekydz86.finance.qualification.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.finance.qualification.dto.AllQualificationsResponse;
import com.sleekydz86.finance.qualification.dto.BasicLivelihoodResponse;
import com.sleekydz86.finance.qualification.dto.HealthInsuranceResponse;
import com.sleekydz86.finance.qualification.dto.MedicalAssistanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QualificationService {

    private final WebClient.Builder webClientBuilder;
    private final PatientService patientService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String MOCK_API_URL = "http://localhost:8085/mock/insurance/eligibility";
    private static final String CACHE_PREFIX_QUALIFICATION = "qualification:patient:";
    private static final long CACHE_TTL_DAYS = 1; // 1일
    private WebClient webClient;

    public QualificationService(WebClient.Builder webClientBuilder, PatientService patientService, RedisTemplate<String, Object> redisTemplate) {
        this.webClientBuilder = webClientBuilder;
        this.patientService = patientService;
        this.redisTemplate = redisTemplate;
        this.webClient = webClientBuilder.baseUrl(MOCK_API_URL).build();
    }

    public Mono<HealthInsuranceResponse> getHealthInsuranceInfo(Long patientNo) {
        String patientRrn = patientService.findPatientRrnByPatientNo(patientNo);
        return getHealthInsuranceInfoByRrn(patientRrn);
    }

    public Mono<HealthInsuranceResponse> getHealthInsuranceInfoByRrn(String patientRrn) {
        return webClient.post()
                .uri("/health")
                .bodyValue(Map.of("patientRrn", patientRrn))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("건강보험 자격 정보를 찾을 수 없습니다."));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(this::mapToHealthInsuranceResponse);
    }

    public Mono<MedicalAssistanceResponse> getMedicalAssistanceInfo(Long patientNo) {
        String patientRrn = patientService.findPatientRrnByPatientNo(patientNo);
        return getMedicalAssistanceInfoByRrn(patientRrn);
    }

    public Mono<MedicalAssistanceResponse> getMedicalAssistanceInfoByRrn(String patientRrn) {
        return webClient.post()
                .uri("/medical-assistance")
                .bodyValue(Map.of("patientRrn", patientRrn))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("의료급여 자격 정보를 찾을 수 없습니다."));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(this::mapToMedicalAssistanceResponse);
    }

    public Mono<BasicLivelihoodResponse> getBasicLivelihoodInfo(Long patientNo) {
        String patientRrn = patientService.findPatientRrnByPatientNo(patientNo);
        return getBasicLivelihoodInfoByRrn(patientRrn);
    }

    public Mono<BasicLivelihoodResponse> getBasicLivelihoodInfoByRrn(String patientRrn) {
        return webClient.post()
                .uri("/basic-livelihood")
                .bodyValue(Map.of("patientRrn", patientRrn))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("기초생활수급자 자격 정보를 찾을 수 없습니다."));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(this::mapToBasicLivelihoodResponse);
    }

    public Mono<AllQualificationsResponse> getAllQualifications(Long patientNo) {
        String cacheKey = CACHE_PREFIX_QUALIFICATION + patientNo;

        AllQualificationsResponse cached = (AllQualificationsResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("자격 정보 캐시 히트: PatientNo={}", patientNo);
            return Mono.just(cached);
        }

        return Mono.zip(
                        getHealthInsuranceInfo(patientNo).onErrorReturn(new HealthInsuranceResponse(false, null, null, null)),
                        getMedicalAssistanceInfo(patientNo).onErrorReturn(new MedicalAssistanceResponse(false, null, null)),
                        getBasicLivelihoodInfo(patientNo).onErrorReturn(new BasicLivelihoodResponse(false, null, null)))
                .map(tuple -> {
                    AllQualificationsResponse response = new AllQualificationsResponse();
                    response.setPatientNo(patientNo);
                    response.setHealthInsurance(tuple.getT1());
                    response.setMedicalAssistance(tuple.getT2());
                    response.setBasicLivelihood(tuple.getT3());

                    redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_DAYS, TimeUnit.DAYS);
                    log.debug("자격 정보 캐시 저장: PatientNo={}", patientNo);

                    return response;
                });
    }

    private HealthInsuranceResponse mapToHealthInsuranceResponse(Map<String, Object> map) {
        HealthInsuranceResponse response = new HealthInsuranceResponse();
        response.setEligible((Boolean) map.getOrDefault("eligible", false));
        response.setType((String) map.get("type"));
        response.setInsuranceNumber((String) map.get("insuranceNumber"));
        response.setInsuranceCompany((String) map.get("insuranceCompany"));
        return response;
    }

    private MedicalAssistanceResponse mapToMedicalAssistanceResponse(Map<String, Object> map) {
        MedicalAssistanceResponse response = new MedicalAssistanceResponse();
        response.setEligible((Boolean) map.getOrDefault("eligible", false));
        response.setType((String) map.get("type"));
        response.setAssistanceNumber((String) map.get("assistanceNumber"));
        return response;
    }

    private BasicLivelihoodResponse mapToBasicLivelihoodResponse(Map<String, Object> map) {
        BasicLivelihoodResponse response = new BasicLivelihoodResponse();
        response.setEligible((Boolean) map.getOrDefault("eligible", false));
        response.setType((String) map.get("type"));
        response.setRecipientNumber((String) map.get("recipientNumber"));
        return response;
    }
}