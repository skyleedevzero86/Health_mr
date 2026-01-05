package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.institution.entity.InstitutionEntity;
import com.sleekydz86.domain.institution.service.InstitutionService;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.finance.medicalfee.api.NonCoveredMedicalFeeApiClient;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemHospDtlResponse;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemHospSummaryResponse;
import com.sleekydz86.finance.medicalfee.api.exception.NonCoveredMedicalFeeApiException;
import com.sleekydz86.finance.medicalfee.entity.MedicalTypeEntity;
import com.sleekydz86.finance.medicalfee.repository.MedicalTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NonCoveredMedicalFeeSyncService {

    private final NonCoveredMedicalFeeApiClient apiClient;
    private final MedicalTypeRepository medicalTypeRepository;
    private final InstitutionService institutionService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX_NON_COVERED_FEE = "noncovered:fee:";
    private static final long CACHE_TTL_HOURS = 24;

    public Mono<Long> getNonCoveredFeeAmountForCurrentInstitution(String npayCd) {
        String cacheKey = CACHE_PREFIX_NON_COVERED_FEE + npayCd + ":" + TenantContext.getTenantId();
        Long cachedAmount = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAmount != null && cachedAmount > 0) {
            log.debug("캐시에서 비급여 금액 조회: NpayCd={}, Amount={}", npayCd, cachedAmount);
            return Mono.just(cachedAmount);
        }

        String currentInstitutionCode = TenantContext.getTenantId();
        if (currentInstitutionCode == null) {
            log.warn("현재 병원 정보가 없습니다. DB 저장 금액 사용");
            return getFeeFromDatabase(npayCd);
        }

        InstitutionEntity institution;
        try {
            institution = institutionService.getInstitutionEntityByCode(currentInstitutionCode);
        } catch (NotFoundException e) {
            institution = null;
        }

        if (institution == null) {
            log.warn("병원 정보를 찾을 수 없습니다. DB 저장 금액 사용");
            return getFeeFromDatabase(npayCd);
        }

        return apiClient.getNonPaymentItemHospDtlList(
                null,
                null,
                null,
                null,
                institution.getInstitutionName(),
                1,
                100
        )
        .map(response -> {
            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                log.warn("API 응답이 비어있습니다. 요약 API로 재시도");
                return null;
            }

            Optional<NonPaymentItemHospDtlResponse.NonPaymentItemHospDtlItem> item = response.getItems().stream()
                    .filter(i -> npayCd.equals(i.getNpayCd()))
                    .filter(i -> institution.getInstitutionName().equals(i.getYadmNm()))
                    .findFirst();

            if (item.isPresent() && item.get().getCurAmt() != null && item.get().getCurAmt() > 0) {
                Long amount = item.get().getCurAmt();
                redisTemplate.opsForValue().set(cacheKey, amount, CACHE_TTL_HOURS, TimeUnit.HOURS);
                log.info("API에서 현재 병원 가격 조회 성공: Institution={}, NpayCd={}, Price={}", 
                        institution.getInstitutionName(), npayCd, amount);
                return amount;
            }

            log.warn("API에서 현재 병원 가격을 찾을 수 없음: Institution={}, NpayCd={}", 
                    institution.getInstitutionName(), npayCd);
            return null;
        })
        .flatMap(amount -> {
            if (amount != null && amount > 0) {
                return Mono.just(amount);
            }

            return getFeeFromSummaryApi(npayCd, institution)
                    .flatMap(summaryAmount -> {
                        if (summaryAmount != null && summaryAmount > 0) {
                            redisTemplate.opsForValue().set(cacheKey, summaryAmount, CACHE_TTL_HOURS, TimeUnit.HOURS);
                            log.info("요약 API에서 평균 가격 조회 성공: NpayCd={}, Price={}", npayCd, summaryAmount);
                            return Mono.just(summaryAmount);
                        }

                        log.warn("API 조회 실패. DB 저장 금액 사용: NpayCd={}", npayCd);
                        return getFeeFromDatabase(npayCd);
                    });
        })
        .onErrorResume(error -> {
            log.error("API 호출 실패: NpayCd={}, Error={}", npayCd, error.getMessage(), error);

            if (error instanceof NonCoveredMedicalFeeApiException apiException) {
                String errorCode = apiException.getErrorCode();

                if ("04".equals(errorCode) || "05".equals(errorCode)) {
                    log.warn("일시적 API 오류. DB 저장 금액 사용: ErrorCode={}", errorCode);
                    return getFeeFromDatabase(npayCd);
                }

                if ("20".equals(errorCode) || "30".equals(errorCode) || "31".equals(errorCode)) {
                    log.error("영구적 API 오류. DB 저장 금액 사용: ErrorCode={}", errorCode);
                    return getFeeFromDatabase(npayCd);
                }

                if ("03".equals(errorCode)) {
                    log.warn("API에 데이터 없음. DB 저장 금액 사용: NpayCd={}", npayCd);
                    return getFeeFromDatabase(npayCd);
                }
            }

            return getFeeFromDatabase(npayCd);
        });
    }

    private Mono<Long> getFeeFromSummaryApi(String npayCd, InstitutionEntity institution) {
        return apiClient.getNonPaymentItemHospList2(
                npayCd,
                null,
                null,
                null,
                institution.getInstitutionName(),
                null,
                1,
                10
        )
        .map(response -> {
            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                return null;
            }

            Optional<NonPaymentItemHospSummaryResponse.NonPaymentItemHospSummaryItem> item = response.getItems().stream()
                    .filter(i -> npayCd.equals(i.getNpayCd()))
                    .findFirst();

            if (item.isPresent()) {
                Long minPrc = item.get().getMinPrc();
                Long maxPrc = item.get().getMaxPrc();

                if (minPrc != null && maxPrc != null && minPrc > 0 && maxPrc > 0) {
                    return (minPrc + maxPrc) / 2;
                }
            }

            return null;
        })
        .onErrorReturn(null);
    }

    private Mono<Long> getFeeFromDatabase(String npayCd) {
        return Mono.fromCallable(() -> {
            MedicalTypeEntity medicalType = medicalTypeRepository.findByMedicalTypeCode(npayCd)
                    .orElse(null);

            if (medicalType != null && medicalType.getMedicalTypeFee() != null) {
                Long amount = medicalType.getMedicalTypeFeeValue();
                log.info("DB에서 기존 금액 조회: NpayCd={}, Amount={}", npayCd, amount);
                return amount != null && amount > 0 ? amount : 0L;
            }

            log.warn("DB에도 금액이 없음: NpayCd={}", npayCd);
            return 0L;
        });
    }

    @Async
    @Transactional
    public void syncMedicalTypeFeeForCurrentInstitution(Long medicalTypeId) {
        MedicalTypeEntity medicalType = medicalTypeRepository.findById(medicalTypeId)
                .orElseThrow(() -> new NotFoundException("진료 유형을 찾을 수 없습니다."));

        Long originalAmount = medicalType.getMedicalTypeFeeValue();

        getNonCoveredFeeAmountForCurrentInstitution(medicalType.getMedicalTypeCode())
                .subscribe(
                        amount -> {
                            if (amount > 0 && amount != originalAmount) {
                                medicalType.updateFee(Money.of(amount));
                                medicalTypeRepository.save(medicalType);
                                log.info("진료 유형 금액 동기화 완료: MedicalTypeId={}, Original={}, New={}", 
                                        medicalTypeId, originalAmount, amount);
                            } else if (amount == 0) {
                                log.warn("API 조회 실패. 기존 금액 유지: MedicalTypeId={}, Amount={}", 
                                        medicalTypeId, originalAmount);
                            } else {
                                log.debug("API 금액과 기존 금액 동일. 업데이트 불필요: MedicalTypeId={}, Amount={}", 
                                        medicalTypeId, amount);
                            }
                        },
                        error -> {
                            log.error("진료 유형 금액 동기화 실패. 기존 금액 유지: MedicalTypeId={}, OriginalAmount={}", 
                                    medicalTypeId, originalAmount, error);
                        }
                );
    }

    @Async
    @Transactional
    public void syncAllMedicalTypeFees() {
        log.info("모든 진료 유형의 비급여 금액 동기화 시작");
        
        try {
            List<MedicalTypeEntity> medicalTypes = medicalTypeRepository.findAll();
            int totalCount = medicalTypes.size();
            int successCount = 0;
            int failCount = 0;

            for (MedicalTypeEntity medicalType : medicalTypes) {
                try {
                    String npayCd = medicalType.getMedicalTypeCode();
                    if (npayCd == null || npayCd.isBlank()) {
                        continue;
                    }

                    getNonCoveredFeeAmountForCurrentInstitution(npayCd)
                            .blockOptional()
                            .ifPresent(amount -> {
                                if (amount > 0) {
                                    Long currentAmount = medicalType.getMedicalTypeFeeValue();
                                    if (currentAmount == null || !currentAmount.equals(amount)) {
                                        medicalType.updateFee(Money.of(amount));
                                        medicalTypeRepository.save(medicalType);
                                        successCount++;
                                        log.debug("진료 유형 금액 동기화: Code={}, Amount={}", npayCd, amount);
                                    }
                                }
                            });
                } catch (Exception e) {
                    failCount++;
                    log.error("진료 유형 금액 동기화 실패: Code={}", medicalType.getMedicalTypeCode(), e);
                }
            }

            log.info("모든 진료 유형의 비급여 금액 동기화 완료: Total={}, Success={}, Fail={}", 
                    totalCount, successCount, failCount);
        } catch (Exception e) {
            log.error("모든 진료 유형의 비급여 금액 동기화 실패", e);
            throw e;
        }
    }

    @Async
    @Transactional
    public void syncMedicalTypeFeeByCode(String npayCd) {
        log.info("특정 비급여 코드의 금액 동기화 시작: NpayCd={}", npayCd);
        
        try {
            MedicalTypeEntity medicalType = medicalTypeRepository.findByMedicalTypeCode(npayCd)
                    .orElse(null);

            if (medicalType == null) {
                log.warn("진료 유형을 찾을 수 없음: NpayCd={}", npayCd);
                return;
            }

            Long originalAmount = medicalType.getMedicalTypeFeeValue();
            Long newAmount = getNonCoveredFeeAmountForCurrentInstitution(npayCd).block();

            if (newAmount != null && newAmount > 0 && !newAmount.equals(originalAmount)) {
                medicalType.updateFee(Money.of(newAmount));
                medicalTypeRepository.save(medicalType);
                log.info("비급여 코드 금액 동기화 완료: NpayCd={}, Original={}, New={}", 
                        npayCd, originalAmount, newAmount);
            } else {
                log.debug("비급여 코드 금액 동기화 불필요: NpayCd={}, Amount={}", npayCd, newAmount);
            }
        } catch (Exception e) {
            log.error("비급여 코드 금액 동기화 실패: NpayCd={}", npayCd, e);
            throw e;
        }
    }

    public Mono<Long> getAveragePrice(String npayCd, String region) {
        return apiClient.getNonPaymentItemSidoCdList(1, 100)
                .map(response -> {
                    if (response.getItems().isEmpty()) {
                        return 0L;
                    }

                    return response.getItems().stream()
                            .filter(item -> npayCd.equals(item.getNpayCd()))
                            .findFirst()
                            .map(item -> {
                                Long avgPrice = switch (region != null ? region : "") {
                                    case "서울" -> item.getPrcAvgSl();
                                    case "부산" -> item.getPrcAvgPs();
                                    case "인천" -> item.getPrcAvgIch();
                                    case "대구" -> item.getPrcAvgTg();
                                    case "광주" -> item.getPrcAvgKw();
                                    case "대전" -> item.getPrcAvgDj();
                                    case "울산" -> item.getPrcAvgUsn();
                                    case "경기" -> item.getPrcAvgKyg();
                                    case "강원" -> item.getPrcAvgKaw();
                                    case "충북" -> item.getPrcAvgCcbk();
                                    case "충남" -> item.getPrcAvgCcn();
                                    case "전북" -> item.getPrcAvgClb();
                                    case "전남" -> item.getPrcAvgCln();
                                    case "경북" -> item.getPrcAvgKsb();
                                    case "경남" -> item.getPrcAvgKsn();
                                    case "제주" -> item.getPrcAvgChj();
                                    case "세종" -> item.getPrcAvgSejong();
                                    default -> item.getPrcAvgAll();
                                };
                                return avgPrice != null ? avgPrice : 0L;
                            })
                            .orElse(0L);
                })
                .onErrorReturn(0L);
    }
}

