package com.sleekydz86.emrclinical.prescription.service;

import com.sleekydz86.emrclinical.prescription.api.AdministrativeActionApiClient;
import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionApiResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionItemResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugAdministrativeActionService {
    
    private final AdministrativeActionApiClient apiClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${drug-administrative-action.api.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${drug-administrative-action.api.cache.ttl:604800}")
    private long cacheTtl;
    
    @Value("${drug-administrative-action.api.cache.prefix:drug:admin-action:}")
    private String cachePrefix;
    
    public AdministrativeActionResponse checkAdministrativeAction(String itemSeq) {
        if (itemSeq == null || itemSeq.isBlank()) {
            return null;
        }
        
        String cacheKey = cachePrefix + itemSeq;
        
        if (cacheEnabled) {
            AdministrativeActionResponse cached = (AdministrativeActionResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("행정처분 정보 캐시 히트: itemSeq={}", itemSeq);
                return cached;
            }
        }
        
        try {
            AdministrativeActionApiResponse apiResponse = apiClient.getActionsByItemSeq(itemSeq).block();
            
            if (apiResponse != null && apiResponse.getResponse() != null && apiResponse.getResponse().isSuccess()) {
                AdministrativeActionResponse response = apiResponse.getResponse();
                
                if (cacheEnabled && response != null) {
                    redisTemplate.opsForValue().set(cacheKey, response, cacheTtl, TimeUnit.SECONDS);
                }
                
                return response;
            }
        } catch (Exception e) {
            log.error("행정처분 정보 조회 실패: itemSeq={}, error={}", itemSeq, e.getMessage(), e);
        }
        
        return null;
    }
    
    public boolean hasActiveAction(String itemSeq) {
        AdministrativeActionResponse response = checkAdministrativeAction(itemSeq);
        if (response == null || response.getItems().isEmpty()) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return response.getItems().stream()
                .anyMatch(item -> {
                    LocalDate endDate = parseDate(item.getRlsEndDate());
                    return endDate != null && endDate.isAfter(today);
                });
    }
    
    public String buildWarningMessage(AdministrativeActionItemResponse action) {
        return String.format(
                "[행정처분 경고] %s - %s (처분일자: %s, 종료일자: %s)",
                action.getAdmDispsName() != null ? action.getAdmDispsName() : "",
                action.getExposeCont() != null ? action.getExposeCont() : "",
                action.getLastSettleDate() != null ? action.getLastSettleDate() : "",
                action.getRlsEndDate() != null ? action.getRlsEndDate() : ""
        );
    }
    
    public WarningLevel classifyWarningLevel(AdministrativeActionItemResponse action) {
        if (action == null || action.getAdmDispsName() == null) {
            return WarningLevel.LOW;
        }
        
        String actionName = action.getAdmDispsName();
        
        if (actionName.contains("제조정지") || actionName.contains("판매정지 6개월")) {
            return WarningLevel.HIGH;
        } else if (actionName.contains("판매정지") || actionName.contains("업무정지")) {
            return WarningLevel.MEDIUM;
        } else {
            return WarningLevel.LOW;
        }
    }
    
    public boolean shouldBlockPrescription(AdministrativeActionItemResponse action) {
        WarningLevel level = classifyWarningLevel(action);
        return level == WarningLevel.HIGH && isActionActive(action);
    }
    
    private boolean isActionActive(AdministrativeActionItemResponse action) {
        if (action == null || action.getRlsEndDate() == null || action.getRlsEndDate().isBlank()) {
            return false;
        }
        
        LocalDate endDate = parseDate(action.getRlsEndDate());
        LocalDate today = LocalDate.now();
        
        return endDate != null && endDate.isAfter(today);
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        
        try {
            if (dateStr.length() == 8) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: dateStr={}", dateStr);
        }
        
        return null;
    }
    
    public enum WarningLevel {
        LOW,
        MEDIUM,
        HIGH
    }
}

