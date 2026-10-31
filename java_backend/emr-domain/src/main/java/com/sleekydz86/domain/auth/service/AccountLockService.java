package com.sleekydz86.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${account.lock.max-fail-attempts:5}")
    private int maxFailAttempts;

    @Value("${account.lock.lock-duration:1800000}")
    private long lockDuration;

    private static final String FAILED_ATTEMPT_PREFIX = "failed_attempt:";
    private static final String LOCKED_ACCOUNT_PREFIX = "locked_account:";


    public void checkAccountLock(String loginId) {
        String lockKey = LOCKED_ACCOUNT_PREFIX + loginId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new com.sleekydz86.core.common.exception.custom.UnauthorizedException(
                    "계정이 잠겼습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public void recordFailedAttempt(String loginId) {
        String attemptKey = FAILED_ATTEMPT_PREFIX + loginId;

        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);
        attempts++;

        redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts),
                1, TimeUnit.HOURS);

        if (attempts >= maxFailAttempts) {
            lockAccount(loginId);
        }
    }

    private void lockAccount(String loginId) {
        String lockKey = LOCKED_ACCOUNT_PREFIX + loginId;
        redisTemplate.opsForValue().set(lockKey, "locked",
                lockDuration, TimeUnit.MILLISECONDS);
    }

    public void clearFailedAttempts(String loginId) {
        String attemptKey = FAILED_ATTEMPT_PREFIX + loginId;
        redisTemplate.delete(attemptKey);
    }

    public void unlockAccount(String loginId) {
        String lockKey = LOCKED_ACCOUNT_PREFIX + loginId;
        redisTemplate.delete(lockKey);
        clearFailedAttempts(loginId);
    }

    public boolean isAccountLocked(String loginId) {
        String lockKey = LOCKED_ACCOUNT_PREFIX + loginId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }
}

