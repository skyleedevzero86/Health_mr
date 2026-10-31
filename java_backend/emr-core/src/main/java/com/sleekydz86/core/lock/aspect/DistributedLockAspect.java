package com.sleekydz86.core.lock.aspect;

import com.sleekydz86.core.lock.annotation.DistributedLock;
import com.sleekydz86.core.lock.service.DistributedLockService;
import com.sleekydz86.core.lock.strategy.KeyGeneratorFactory;
import com.sleekydz86.core.lock.strategy.LockKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final DistributedLockService distributedLockService;
    private final KeyGeneratorFactory keyGeneratorFactory;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String lockKey = generateLockKey(distributedLock, method, args);
        log.debug("분산 락 시도: method={}, lockKey={}", method.getName(), lockKey);

        String lockValue = null;
        try {
            lockValue = distributedLockService.tryLock(
                    lockKey,
                    distributedLock.waitTime(),
                    distributedLock.leaseTime()
            );
        } catch (RuntimeException exception) {
            log.warn("분산 락 저장소 오류, 비즈니스 로직을 계속 진행합니다: method={}", method.getName(), exception);
            return joinPoint.proceed();
        }

        if (lockValue == null) {
            log.warn("락 획득 실패, 비즈니스 로직을 계속 진행합니다: method={}, lockKey={}", method.getName(), lockKey);
            return joinPoint.proceed();
        }

        try {
            log.debug("락 획득 성공, 비즈니스 로직 실행: method={}, lockKey={}", method.getName(), lockKey);
            return joinPoint.proceed();
        } finally {
            try {
                boolean released = distributedLockService.unlock(lockKey, lockValue);
                if (released) {
                    log.debug("락 해제 완료: method={}, lockKey={}", method.getName(), lockKey);
                } else {
                    log.warn("락 해제 실패: method={}, lockKey={}", method.getName(), lockKey);
                }
            } catch (RuntimeException exception) {
                log.warn("락 해제 중 오류: method={}, lockKey={}", method.getName(), lockKey, exception);
            }
        }
    }

    private String generateLockKey(DistributedLock distributedLock, Method method, Object[] args) {
        String keyExpression = distributedLock.key();

        if (distributedLock.keyType() == DistributedLock.LockKeyType.CUSTOM) {
            return keyExpression;
        }

        LockKeyGenerator generator = keyGeneratorFactory.getLockKeyGenerator(distributedLock.keyType());
        return generator.generate(keyExpression, method, args);
    }
}
