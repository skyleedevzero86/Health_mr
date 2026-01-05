package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.finance.medicalfee.api.NonCoveredMedicalFeeApiClient;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemClcdListResponse;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemSidoCdListResponse;
import com.sleekydz86.finance.medicalfee.dto.NonPaymentItemStatisticsByTypeResponse;
import com.sleekydz86.finance.medicalfee.dto.NonPaymentItemStatisticsByRegionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NonCoveredMedicalFeeStatisticsService {

    private final NonCoveredMedicalFeeApiClient apiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${non-covered-medical-fee.cache.stat-ttl:86400}")
    private long statTtl;

    private static final String CACHE_PREFIX_STAT_TYPE = "noncovered:stat:type:";
    private static final String CACHE_PREFIX_STAT_REGION = "noncovered:stat:region:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Mono<NonPaymentItemStatisticsByTypeResponse> getStatisticsByInstitutionType(String npayCd) {
        String cacheKey = CACHE_PREFIX_STAT_TYPE + npayCd;
        
        @SuppressWarnings("unchecked")
        NonPaymentItemStatisticsByTypeResponse cached = 
                (NonPaymentItemStatisticsByTypeResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return apiClient.getNonPaymentItemClcdList(1, 100)
                .map(response -> {
                    if (response.getItems().isEmpty()) {
                        return NonPaymentItemStatisticsByTypeResponse.builder()
                                .npayCd(npayCd)
                                .build();
                    }

                    NonPaymentItemClcdListResponse.NonPaymentItemClcdItem item = response.getItems().stream()
                            .filter(i -> npayCd.equals(i.getNpayCd()))
                            .findFirst()
                            .orElse(response.getItems().get(0));

                    LocalDate stdDate = parseDate(item.getStdDate());

                    NonPaymentItemStatisticsByTypeResponse result = NonPaymentItemStatisticsByTypeResponse.builder()
                            .npayCd(item.getNpayCd())
                            .npayKorNm(item.getNpayKorNm())
                            .stdDate(stdDate)
                            .all(buildPriceStatistics(item.getPrcMinAll(), item.getPrcMaxAll(), 
                                    item.getPrcAvgAll(), item.getMiddAvgAll()))
                            .usgh(buildPriceStatistics(item.getPrcMinUsgh(), item.getPrcMaxUsgh(), 
                                    item.getPrcAvgUsgh(), item.getMiddAvgUsgh()))
                            .gnhp(buildPriceStatistics(item.getPrcMinGnhp(), item.getPrcMaxGnhp(), 
                                    item.getPrcAvgGnhp(), item.getMiddAvgGnhp()))
                            .hosp(buildPriceStatistics(item.getPrcMinHosp(), item.getPrcMaxHosp(), 
                                    item.getPrcAvgHosp(), item.getMiddAvgHosp()))
                            .cmdc(buildPriceStatistics(item.getPrcMinCmdc(), item.getPrcMaxCmdc(), 
                                    item.getPrcAvgCmdc(), item.getMiddAvgCmdc()))
                            .dety(buildPriceStatistics(item.getPrcMinDety(), item.getPrcMaxDety(), 
                                    item.getPrcAvgDety(), item.getMiddAvgDety()))
                            .recu(buildPriceStatistics(item.getPrcMinRecu(), item.getPrcMaxRecu(), 
                                    item.getPrcAvgRecu(), item.getMiddAvgRecu()))
                            .build();

                    redisTemplate.opsForValue().set(cacheKey, result, statTtl, TimeUnit.SECONDS);
                    return result;
                });
    }

    public Mono<NonPaymentItemStatisticsByRegionResponse> getStatisticsByRegion(String npayCd) {
        String cacheKey = CACHE_PREFIX_STAT_REGION + npayCd;
        
        @SuppressWarnings("unchecked")
        NonPaymentItemStatisticsByRegionResponse cached = 
                (NonPaymentItemStatisticsByRegionResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return apiClient.getNonPaymentItemSidoCdList(1, 100)
                .map(response -> {
                    if (response.getItems().isEmpty()) {
                        return NonPaymentItemStatisticsByRegionResponse.builder()
                                .npayCd(npayCd)
                                .regionStatistics(new HashMap<>())
                                .build();
                    }

                    NonPaymentItemSidoCdListResponse.NonPaymentItemSidoCdItem item = response.getItems().stream()
                            .filter(i -> npayCd.equals(i.getNpayCd()))
                            .findFirst()
                            .orElse(response.getItems().get(0));

                    LocalDate stdDate = parseDate(item.getStdDate());
                    Map<String, NonPaymentItemStatisticsByRegionResponse.PriceStatistics> regionStats = new HashMap<>();

                    regionStats.put("서울", buildPriceStatistics(item.getPrcMinSl(), item.getPrcMaxSl(), 
                            item.getPrcAvgSl(), item.getMiddAvgSl()));
                    regionStats.put("부산", buildPriceStatistics(item.getPrcMinPs(), item.getPrcMaxPs(), 
                            item.getPrcAvgPs(), item.getMiddAvgPs()));
                    regionStats.put("인천", buildPriceStatistics(item.getPrcMinIch(), item.getPrcMaxIch(), 
                            item.getPrcAvgIch(), item.getMiddAvgIch()));
                    regionStats.put("대구", buildPriceStatistics(item.getPrcMinTg(), item.getPrcMaxTg(), 
                            item.getPrcAvgTg(), item.getMiddAvgTg()));
                    regionStats.put("광주", buildPriceStatistics(item.getPrcMinKw(), item.getPrcMaxKw(), 
                            item.getPrcAvgKw(), item.getMiddAvgKw()));
                    regionStats.put("대전", buildPriceStatistics(item.getPrcMinDj(), item.getPrcMaxDj(), 
                            item.getPrcAvgDj(), item.getMiddAvgDj()));
                    regionStats.put("울산", buildPriceStatistics(item.getPrcMinUsn(), item.getPrcMaxUsn(), 
                            item.getPrcAvgUsn(), item.getMiddAvgUsn()));
                    regionStats.put("경기", buildPriceStatistics(item.getPrcMinKyg(), item.getPrcMaxKyg(), 
                            item.getPrcAvgKyg(), item.getMiddAvgKyg()));
                    regionStats.put("강원", buildPriceStatistics(item.getPrcMinKaw(), item.getPrcMaxKaw(), 
                            item.getPrcAvgKaw(), item.getMiddAvgKaw()));
                    regionStats.put("충북", buildPriceStatistics(item.getPrcMinCcbk(), item.getPrcMaxCcbk(), 
                            item.getPrcAvgCcbk(), item.getMiddAvgCcbk()));
                    regionStats.put("충남", buildPriceStatistics(item.getPrcMinCcn(), item.getPrcMaxCcn(), 
                            item.getPrcAvgCcn(), item.getMiddAvgCcn()));
                    regionStats.put("전북", buildPriceStatistics(item.getPrcMinClb(), item.getPrcMaxClb(), 
                            item.getPrcAvgClb(), item.getMiddAvgClb()));
                    regionStats.put("전남", buildPriceStatistics(item.getPrcMinCln(), item.getPrcMaxCln(), 
                            item.getPrcAvgCln(), item.getMiddAvgCln()));
                    regionStats.put("경북", buildPriceStatistics(item.getPrcMinKsb(), item.getPrcMaxKsb(), 
                            item.getPrcAvgKsb(), item.getMiddAvgKsb()));
                    regionStats.put("경남", buildPriceStatistics(item.getPrcMinKsn(), item.getPrcMaxKsn(), 
                            item.getPrcAvgKsn(), item.getMiddAvgKsn()));
                    regionStats.put("제주", buildPriceStatistics(item.getPrcMinChj(), item.getPrcMaxChj(), 
                            item.getPrcAvgChj(), item.getMiddAvgChj()));
                    regionStats.put("세종", buildPriceStatistics(item.getPrcMinSejong(), item.getPrcMaxSejong(), 
                            item.getPrcAvgSejong(), item.getMiddAvgSejong()));

                    NonPaymentItemStatisticsByRegionResponse result = 
                            NonPaymentItemStatisticsByRegionResponse.builder()
                                    .npayCd(item.getNpayCd())
                                    .npayKorNm(item.getNpayKorNm())
                                    .stdDate(stdDate)
                                    .regionStatistics(regionStats)
                                    .build();

                    redisTemplate.opsForValue().set(cacheKey, result, statTtl, TimeUnit.SECONDS);
                    return result;
                });
    }

    public Mono<Map<String, Object>> getPriceComparison(String npayCd, String region, String institutionType) {
        return Mono.zip(
                getStatisticsByInstitutionType(npayCd),
                getStatisticsByRegion(npayCd)
        ).map(tuple -> {
            NonPaymentItemStatisticsByTypeResponse typeStats = tuple.getT1();
            NonPaymentItemStatisticsByRegionResponse regionStats = tuple.getT2();

            Map<String, Object> comparison = new HashMap<>();
            comparison.put("npayCd", npayCd);
            comparison.put("npayKorNm", typeStats.getNpayKorNm());

            if (institutionType != null) {
                NonPaymentItemStatisticsByTypeResponse.PriceStatistics typeStat = switch (institutionType) {
                    case "usgh" -> typeStats.getUsgh();
                    case "gnhp" -> typeStats.getGnhp();
                    case "hosp" -> typeStats.getHosp();
                    case "cmdc" -> typeStats.getCmdc();
                    case "dety" -> typeStats.getDety();
                    case "recu" -> typeStats.getRecu();
                    default -> typeStats.getAll();
                };
                comparison.put("institutionType", typeStat);
            }

            if (region != null && regionStats.getRegionStatistics() != null) {
                NonPaymentItemStatisticsByRegionResponse.PriceStatistics regionStat = 
                        regionStats.getRegionStatistics().get(region);
                comparison.put("region", regionStat);
            }

            NonPaymentItemStatisticsByTypeResponse.PriceStatistics allStats = typeStats.getAll();
            if (allStats != null) {
                Long recommendedMin = allStats.getMinPrice();
                Long recommendedMax = allStats.getMaxPrice();
                if (recommendedMin != null && recommendedMax != null) {
                    comparison.put("recommendedPriceRange", Map.of(
                            "min", recommendedMin,
                            "max", recommendedMax,
                            "avg", allStats.getAvgPrice()
                    ));
                }
            }

            return comparison;
        });
    }

    private NonPaymentItemStatisticsByTypeResponse.PriceStatistics buildPriceStatistics(
            Long min, Long max, Long avg, Long middle) {
        return NonPaymentItemStatisticsByTypeResponse.PriceStatistics.builder()
                .minPrice(min)
                .maxPrice(max)
                .avgPrice(avg)
                .middlePrice(middle)
                .build();
    }

    private NonPaymentItemStatisticsByRegionResponse.PriceStatistics buildPriceStatistics(
            Long min, Long max, Long avg, Long middle) {
        return NonPaymentItemStatisticsByRegionResponse.PriceStatistics.builder()
                .minPrice(min)
                .maxPrice(max)
                .avgPrice(avg)
                .middlePrice(middle)
                .build();
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

