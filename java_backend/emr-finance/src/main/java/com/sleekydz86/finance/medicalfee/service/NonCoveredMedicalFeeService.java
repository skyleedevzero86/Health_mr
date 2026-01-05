package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.finance.medicalfee.api.NonCoveredMedicalFeeApiClient;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemCodeList2Response;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemHospDtlResponse;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemHospSummaryResponse;
import com.sleekydz86.finance.medicalfee.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NonCoveredMedicalFeeService {

    private final NonCoveredMedicalFeeApiClient apiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${non-covered-medical-fee.cache.code-ttl:604800}")
    private long codeTtl;

    @Value("${non-covered-medical-fee.cache.hosp-ttl:86400}")
    private long hospTtl;

    private static final String CACHE_PREFIX_CODE = "noncovered:code:";
    private static final String CACHE_PREFIX_HOSP_DETAIL = "noncovered:hosp:detail:";
    private static final String CACHE_PREFIX_HOSP_SUMMARY = "noncovered:hosp:summary:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Mono<Map<String, Object>> getNonPaymentItemCodes(NonPaymentItemCodeRequest request) {
        String cacheKey = buildCodeCacheKey(request);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return apiClient.getNonPaymentItemCodeList2(request.getPageNo(), request.getNumOfRows())
                .map(response -> {
                    List<NonPaymentItemCodeResponse> items = response.getItems().stream()
                            .map(item -> {
                                LocalDate adtFrDd = parseDate(item.getAdtFrDd());
                                LocalDate adtEndDd = parseDate(item.getAdtEndDd());
                                
                                return NonPaymentItemCodeResponse.builder()
                                        .npayCd(item.getNpayCd())
                                        .npayKorNm(item.getNpayKorNm())
                                        .npayMdivCd(item.getNpayMdivCd())
                                        .npayMdivCdNm(item.getNpayMdivCdNm())
                                        .npaySdivCd(item.getNpaySdivCd())
                                        .npaySdivCdNm(item.getNpaySdivCdNm())
                                        .npayDtlDivCd(item.getNpayDtlDivCd())
                                        .npayDtlDivCdNm(item.getNpayDtlDivCdNm())
                                        .cmmtTxt(item.getCmmtTxt())
                                        .adtFrDd(adtFrDd)
                                        .adtEndDd(adtEndDd)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    Map<String, Object> result = Map.of(
                            "items", items,
                            "totalCount", response.getTotalCount(),
                            "pageNo", request.getPageNo(),
                            "numOfRows", request.getNumOfRows()
                    );

                    redisTemplate.opsForValue().set(cacheKey, result, codeTtl, TimeUnit.SECONDS);
                    return result;
                });
    }

    public Mono<Map<String, Object>> getNonPaymentItemHospDetail(NonPaymentItemHospSearchRequest request) {
        String cacheKey = buildHospDetailCacheKey(request);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return apiClient.getNonPaymentItemHospDtlList(
                        request.getYkiho(),
                        request.getClCd(),
                        request.getSidoCd(),
                        request.getSgguCd(),
                        request.getYadmNm(),
                        request.getPageNo(),
                        request.getNumOfRows()
                )
                .map(response -> {
                    List<NonPaymentItemHospDetailResponse> items = response.getItems().stream()
                            .map(item -> {
                                LocalDate adtFrDd = parseDate(item.getAdtFrDd());
                                LocalDate adtEndDd = parseDate(item.getAdtEndDd());
                                
                                return NonPaymentItemHospDetailResponse.builder()
                                        .ykiho(item.getYkiho())
                                        .yadmNm(item.getYadmNm())
                                        .clCd(item.getClCd())
                                        .clCdNm(item.getClCdNm())
                                        .sidoCd(item.getSidoCd())
                                        .sidoCdNm(item.getSidoCdNm())
                                        .sgguCd(item.getSgguCd())
                                        .sgguCdNm(item.getSgguCdNm())
                                        .npayCd(item.getNpayCd())
                                        .npayKorNm(item.getNpayKorNm())
                                        .yadmNpayCdNm(item.getYadmNpayCdNm())
                                        .adtFrDd(adtFrDd)
                                        .adtEndDd(adtEndDd)
                                        .curAmt(item.getCurAmt())
                                        .build();
                            })
                            .sorted((a, b) -> {
                                if (a.getCurAmt() == null && b.getCurAmt() == null) return 0;
                                if (a.getCurAmt() == null) return 1;
                                if (b.getCurAmt() == null) return -1;
                                return Long.compare(a.getCurAmt(), b.getCurAmt());
                            })
                            .collect(Collectors.toList());

                    Map<String, Object> result = Map.of(
                            "items", items,
                            "totalCount", response.getTotalCount(),
                            "pageNo", request.getPageNo(),
                            "numOfRows", request.getNumOfRows()
                    );

                    redisTemplate.opsForValue().set(cacheKey, result, hospTtl, TimeUnit.SECONDS);
                    return result;
                });
    }

    public Mono<Map<String, Object>> getNonPaymentItemHospSummary(NonPaymentItemHospSearchRequest request) {
        String cacheKey = buildHospSummaryCacheKey(request);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return apiClient.getNonPaymentItemHospList2(
                        request.getNpayCd(),
                        request.getClCd(),
                        request.getSidoCd(),
                        request.getSgguCd(),
                        request.getYadmNm(),
                        request.getSearchWrd(),
                        request.getPageNo(),
                        request.getNumOfRows()
                )
                .map(response -> {
                    List<NonPaymentItemHospSummaryResponse> items = response.getItems().stream()
                            .map(item -> {
                                LocalDate adtFrDd = parseDate(item.getAdtFrDd());
                                LocalDate adtEndDd = parseDate(item.getAdtEndDd());
                                
                                Long avgPrc = null;
                                if (item.getMinPrc() != null && item.getMaxPrc() != null) {
                                    avgPrc = (item.getMinPrc() + item.getMaxPrc()) / 2;
                                }
                                
                                return NonPaymentItemHospSummaryResponse.builder()
                                        .ykiho(item.getYkiho())
                                        .yadmNm(item.getYadmNm())
                                        .clCd(item.getClCd())
                                        .clCdNm(item.getClCdNm())
                                        .sidoCd(item.getSidoCd())
                                        .sidoCdNm(item.getSidoCdNm())
                                        .sgguCd(item.getSgguCd())
                                        .sgguCdNm(item.getSgguCdNm())
                                        .npayCd(item.getNpayCd())
                                        .npayKorNm(item.getNpayKorNm())
                                        .adtFrDd(adtFrDd)
                                        .adtEndDd(adtEndDd)
                                        .minPrc(item.getMinPrc())
                                        .maxPrc(item.getMaxPrc())
                                        .avgPrc(avgPrc)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    Map<String, Object> result = Map.of(
                            "items", items,
                            "totalCount", response.getTotalCount(),
                            "pageNo", request.getPageNo(),
                            "numOfRows", request.getNumOfRows()
                    );

                    redisTemplate.opsForValue().set(cacheKey, result, hospTtl, TimeUnit.SECONDS);
                    return result;
                });
    }

    public Mono<List<NonPaymentItemHospDetailResponse>> searchHospitalsByItem(
            String npayCd, String sidoCd, String clCd) {
        NonPaymentItemHospSearchRequest request = NonPaymentItemHospSearchRequest.builder()
                .npayCd(npayCd)
                .sidoCd(sidoCd)
                .clCd(clCd)
                .pageNo(1)
                .numOfRows(100)
                .build();

        return getNonPaymentItemHospDetail(request)
                .map(result -> (List<NonPaymentItemHospDetailResponse>) result.get("items"));
    }

    private String buildCodeCacheKey(NonPaymentItemCodeRequest request) {
        return CACHE_PREFIX_CODE + request.getPageNo() + ":" + request.getNumOfRows() + ":" +
                (request.getSearchKeyword() != null ? request.getSearchKeyword() : "");
    }

    private String buildHospDetailCacheKey(NonPaymentItemHospSearchRequest request) {
        return CACHE_PREFIX_HOSP_DETAIL + 
                (request.getYkiho() != null ? request.getYkiho() : "") + ":" +
                (request.getClCd() != null ? request.getClCd() : "") + ":" +
                (request.getSidoCd() != null ? request.getSidoCd() : "") + ":" +
                request.getPageNo();
    }

    private String buildHospSummaryCacheKey(NonPaymentItemHospSearchRequest request) {
        return CACHE_PREFIX_HOSP_SUMMARY + 
                (request.getNpayCd() != null ? request.getNpayCd() : "") + ":" +
                (request.getClCd() != null ? request.getClCd() : "") + ":" +
                (request.getSidoCd() != null ? request.getSidoCd() : "") + ":" +
                request.getPageNo();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }
}

