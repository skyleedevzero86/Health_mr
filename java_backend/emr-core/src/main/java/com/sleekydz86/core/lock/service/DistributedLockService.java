package com.sleekydz86.core.lock.service;

import com.sleekydz86.core.lock.script.LuaScriptProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private final LuaScriptProvider luaScriptProvider;
    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_VALUE_PREFIX = "lock_value:";
    private static final long RETRY_INTERVAL_MS = 100L;
    private static final long MILLISECONDS_PER_SECOND = 1000L;

    public String tryLock(String key, long waitTime, long leaseTime) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = LOCK_VALUE_PREFIX + UUID.randomUUID().toString();
        long endTime = System.currentTimeMillis() + (waitTime * MILLISECONDS_PER_SECOND);

        while (System.currentTimeMillis() < endTime) {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, leaseTime, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(acquired)) {
                log.debug("락 획득 성공: key={}, value={}", lockKey, lockValue);
                return lockValue;
            }

            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("락 획득 대기 중 인터럽트 발생: key={}", lockKey);
                return null;
            }
        }

        log.warn("락 획득 실패 (시간 초과): key={}, waitTime={}초", lockKey, waitTime);
        return null;
    }

    public boolean unlock(String key, String lockValue) {
        String lockKey = LOCK_PREFIX + key;
        DefaultRedisScript<Long> script = createScript(luaScriptProvider.getUnlockScript());

        Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);

        boolean released = result != null && result > 0;
        if (released) {
            log.debug("락 해제 성공: key={}, value={}", lockKey, lockValue);
        } else {
            log.warn("락 해제 실패 (값 불일치 또는 이미 해제됨): key={}, value={}", lockKey, lockValue);
        }

        return released;
    }

    public boolean extendLock(String key, String lockValue, long additionalTime) {
        String lockKey = LOCK_PREFIX + key;
        DefaultRedisScript<Long> script = createScript(luaScriptProvider.getExtendLockScript());

        Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), 
                lockValue, String.valueOf(additionalTime));

        boolean extended = result != null && result > 0;
        if (extended) {
            log.debug("락 연장 성공: key={}, additionalTime={}초", lockKey, additionalTime);
        }

        return extended;
    }

    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    private DefaultRedisScript<Long> createScript(String scriptText) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(scriptText);
        script.setResultType(Long.class);
        return script;
    }
}
