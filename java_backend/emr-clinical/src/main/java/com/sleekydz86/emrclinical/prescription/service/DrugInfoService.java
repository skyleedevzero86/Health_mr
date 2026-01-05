package com.sleekydz86.emrclinical.prescription.service;

import com.sleekydz86.emrclinical.prescription.api.DrugInfoApiClient;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoApiResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoItemResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoSearchRequest;
import com.sleekydz86.emrclinical.prescription.api.exception.DrugInfoApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugInfoService {

    private final DrugInfoApiClient apiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${drug-info.api.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${drug-info.api.cache.ttl:86400}")
    private long cacheTtl; // 초 단위

    @Value("${drug-info.api.cache.prefix:drug:info:}")
    private String cachePrefix;

    public List<DrugInfoItemResponse> searchDrugInfo(DrugInfoSearchRequest request) {
        try {
            String cacheKey = buildCacheKey(request);

            if (cacheEnabled) {
                @SuppressWarnings("unchecked")
                List<DrugInfoItemResponse> cached = (List<DrugInfoItemResponse>) redisTemplate.opsForValue()
                        .get(cacheKey);
                if (cached != null) {
                    log.debug("Drug Info 캐시 히트: key={}", cacheKey);
                    return cached;
                }
            }

            DrugInfoApiResponse apiResponse = apiClient.searchDrugInfo(request).block();

            if (apiResponse == null || apiResponse.getResponse() == null) {
                log.warn("Drug Info API 응답이 null입니다.");
                return Collections.emptyList();
            }

            if (!apiResponse.getResponse().isSuccess()) {
                String errorMsg = apiResponse.getResponse().getHeader() != null
                        ? apiResponse.getResponse().getHeader().getResultMsg()
                        : "알 수 없는 오류";
                log.error("Drug Info API 오류: {}", errorMsg);
                throw new DrugInfoApiException("의약품 정보 조회 실패: " + errorMsg);
            }

            List<DrugInfoItemResponse> items = apiResponse.getResponse().getItems();

            if (cacheEnabled && items != null && !items.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, items, Duration.ofSeconds(cacheTtl));
                log.debug("Drug Info 캐시 저장: key={}, count={}", cacheKey, items.size());
            }

            return items != null ? items : Collections.emptyList();

        } catch (Exception e) {
            log.error("Drug Info 검색 실패", e);
            if (e instanceof DrugInfoApiException) {
                throw e;
            }
            throw new DrugInfoApiException("의약품 정보 검색 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public DrugInfoItemResponse getDrugInfoByItemSeq(String itemSeq) {
        if (itemSeq == null || itemSeq.isBlank()) {
            throw new DrugInfoApiException("품목기준코드는 필수입니다.");
        }

        String cacheKey = cachePrefix + "itemSeq:" + itemSeq;

        if (cacheEnabled) {
            @SuppressWarnings("unchecked")
            DrugInfoItemResponse cached = (DrugInfoItemResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Drug Info 캐시 히트: key={}", cacheKey);
                return cached;
            }
        }

        DrugInfoApiResponse apiResponse = apiClient.getDrugInfoByItemSeq(itemSeq).block();

        if (apiResponse == null || apiResponse.getResponse() == null) {
            log.warn("Drug Info API 응답이 null입니다. itemSeq={}", itemSeq);
            return null;
        }

        if (!apiResponse.getResponse().isSuccess()) {
            String errorMsg = apiResponse.getResponse().getHeader() != null
                    ? apiResponse.getResponse().getHeader().getResultMsg()
                    : "알 수 없는 오류";
            log.error("Drug Info API 오류: itemSeq={}, error={}", itemSeq, errorMsg);

            if ("03".equals(apiResponse.getResponse().getHeader().getResultCode())) {
                return null;
            }

            throw new DrugInfoApiException("의약품 정보 조회 실패: " + errorMsg);
        }

        List<DrugInfoItemResponse> items = apiResponse.getResponse().getItems();
        DrugInfoItemResponse item = (items != null && !items.isEmpty()) ? items.get(0) : null;

        if (cacheEnabled && item != null) {
            redisTemplate.opsForValue().set(cacheKey, item, Duration.ofSeconds(cacheTtl));
            log.debug("Drug Info 캐시 저장: key={}", cacheKey);
        }

        return item;
    }

    public List<DrugInfoItemResponse> searchDrugInfoByItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new DrugInfoApiException("제품명은 필수입니다.");
        }

        DrugInfoSearchRequest request = DrugInfoSearchRequest.builder()
                .itemName(itemName)
                .pageNo(1)
                .numOfRows(10)
                .build();

        return searchDrugInfo(request);
    }

    public boolean validateDrugCode(String drugCode) {
        if (drugCode == null || drugCode.isBlank()) {
            return false;
        }

        DrugInfoItemResponse drugInfo = getDrugInfoByItemSeq(drugCode);
        return drugInfo != null;
    }

    public List<String> checkDrugInteractions(List<String> drugCodes) {
        if (drugCodes == null || drugCodes.size() < 2) {
            return Collections.emptyList();
        }

        return drugCodes.stream()
                .map(this::getDrugInfoByItemSeq)
                .filter(item -> item != null && item.getIntrcQesitm() != null && !item.getIntrcQesitm().isBlank())
                .map(DrugInfoItemResponse::getIntrcQesitm)
                .collect(Collectors.toList());
    }

    public String buildSpecialNote(DrugInfoItemResponse drugInfo) {
        if (drugInfo == null) {
            return null;
        }

        StringBuilder note = new StringBuilder();

        if (drugInfo.getAtpnWarnQesitm() != null && !drugInfo.getAtpnWarnQesitm().isBlank()) {
            note.append("[주의사항 경고]\n").append(drugInfo.getAtpnWarnQesitm()).append("\n\n");
        }

        if (drugInfo.getAtpnQesitm() != null && !drugInfo.getAtpnQesitm().isBlank()) {
            note.append("[주의사항]\n").append(drugInfo.getAtpnQesitm()).append("\n\n");
        }

        if (drugInfo.getSeQesitm() != null && !drugInfo.getSeQesitm().isBlank()) {
            note.append("[부작용]\n").append(drugInfo.getSeQesitm());
        }

        return note.length() > 0 ? note.toString() : null;
    }

    private String buildCacheKey(DrugInfoSearchRequest request) {
        StringBuilder key = new StringBuilder(cachePrefix);

        if (request.getItemSeq() != null && !request.getItemSeq().isBlank()) {
            key.append("itemSeq:").append(request.getItemSeq());
        } else if (request.getItemName() != null && !request.getItemName().isBlank()) {
            key.append("itemName:").append(request.getItemName());
        } else if (request.getEntpName() != null && !request.getEntpName().isBlank()) {
            key.append("entpName:").append(request.getEntpName());
        } else {
            key.append("search");
        }

        if (request.getPageNo() != null) {
            key.append(":page:").append(request.getPageNo());
        }

        return key.toString();
    }
}