package com.sleekydz86.core.lock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final String PROCESSING_VALUE = "processing";

    public boolean checkAndSet(String key, long ttl) {
        String idempotencyKey = IDEMPOTENCY_PREFIX + key;
        
        Boolean setIfAbsent = redisTemplate.opsForValue()
                .setIfAbsent(idempotencyKey, PROCESSING_VALUE, ttl, TimeUnit.SECONDS);

        boolean isDuplicate = !Boolean.TRUE.equals(setIfAbsent);
        
        if (isDuplicate) {
            log.warn("중복 요청 감지: key={}", idempotencyKey);
        } else {
            log.debug("멱등성 키 생성: key={}, ttl={}초", idempotencyKey, ttl);
        }

        return isDuplicate;
    }

    public void saveResult(String key, Object result, long ttl) {
        try {
            String idempotencyKey = IDEMPOTENCY_PREFIX + key;
            String resultJson = objectMapper.writeValueAsString(result);
            
            redisTemplate.opsForValue().set(idempotencyKey, resultJson, ttl, TimeUnit.SECONDS);
            log.debug("멱등성 결과 저장: key={}", idempotencyKey);
        } catch (JsonProcessingException e) {
            log.error("멱등성 결과 저장 실패: key={}", key, e);
        }
    }

    public <T> T getPreviousResult(String key, Class<T> resultType) {
        try {
            String idempotencyKey = IDEMPOTENCY_PREFIX + key;
            String resultJson = redisTemplate.opsForValue().get(idempotencyKey);
            
            if (resultJson != null && !resultJson.equals(PROCESSING_VALUE)) {
                log.debug("이전 결과 조회: key={}", idempotencyKey);
                return objectMapper.readValue(resultJson, resultType);
            }
        } catch (JsonProcessingException e) {
            log.error("이전 결과 조회 실패: key={}", key, e);
        }
        
        return null;
    }

    public void delete(String key) {
        String idempotencyKey = IDEMPOTENCY_PREFIX + key;
        redisTemplate.delete(idempotencyKey);
        log.debug("멱등성 키 삭제: key={}", idempotencyKey);
    }

    public boolean exists(String key) {
        String idempotencyKey = IDEMPOTENCY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey));
    }
}
