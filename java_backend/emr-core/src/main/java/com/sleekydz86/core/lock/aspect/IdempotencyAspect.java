package com.sleekydz86.core.lock.aspect;

import com.sleekydz86.core.lock.annotation.Idempotent;
import com.sleekydz86.core.lock.exception.IdempotencyException;
import com.sleekydz86.core.lock.service.IdempotencyService;
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
@Order(2)
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;
    private final KeyGeneratorFactory keyGeneratorFactory;
    private static final long RETRY_WAIT_MS = 500L;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String idempotencyKey = generateIdempotencyKey(idempotent, method, args);
        log.debug("멱등성 체크: method={}, key={}", method.getName(), idempotencyKey);

        Object previousResult = idempotencyService.getPreviousResult(
                idempotencyKey, 
                method.getReturnType()
        );

        if (previousResult != null) {
            log.info("중복 요청 감지, 이전 결과 반환: method={}, key={}", method.getName(), idempotencyKey);
            
            if (idempotent.throwExceptionOnDuplicate()) {
                throw new IdempotencyException(
                        "중복 요청입니다. 동일한 요청이 이미 처리되었습니다. key: " + idempotencyKey);
            } else {
                return previousResult;
            }
        }

        boolean isDuplicate = idempotencyService.checkAndSet(idempotencyKey, idempotent.ttl());
        
        if (isDuplicate) {
            log.warn("동시 중복 요청 감지: method={}, key={}", method.getName(), idempotencyKey);
            
            if (idempotent.throwExceptionOnDuplicate()) {
                throw new IdempotencyException(
                        "중복 요청입니다. 동일한 요청이 처리 중입니다. key: " + idempotencyKey);
            } else {
                try {
                    Thread.sleep(RETRY_WAIT_MS);
                    previousResult = idempotencyService.getPreviousResult(
                            idempotencyKey, 
                            method.getReturnType()
                    );
                    if (previousResult != null) {
                        return previousResult;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        try {
            Object result = joinPoint.proceed();
            idempotencyService.saveResult(idempotencyKey, result, idempotent.ttl());
            log.debug("멱등성 처리 완료: method={}, key={}", method.getName(), idempotencyKey);
            return result;
        } catch (Exception e) {
            idempotencyService.delete(idempotencyKey);
            log.error("비즈니스 로직 실행 실패, 멱등성 키 삭제: method={}, key={}", 
                    method.getName(), idempotencyKey, e);
            throw e;
        }
    }

    private String generateIdempotencyKey(Idempotent idempotent, Method method, Object[] args) {
        String keyExpression = idempotent.key();

        if (keyExpression == null || keyExpression.isEmpty()) {
            return method.getDeclaringClass().getName() + ":" + method.getName() + ":" + 
                   generateDefaultKey(method, args);
        }

        if (idempotent.keyType() == Idempotent.IdempotencyKeyType.CUSTOM) {
            return keyExpression;
        }

        if (idempotent.keyType() == Idempotent.IdempotencyKeyType.HEADER) {
            LockKeyGenerator generator = keyGeneratorFactory.getIdempotencyKeyGenerator(
                    Idempotent.IdempotencyKeyType.HEADER);
            return generator.generate(idempotent.headerName(), method, args);
        }

        LockKeyGenerator generator = keyGeneratorFactory.getIdempotencyKeyGenerator(idempotent.keyType());
        return generator.generate(keyExpression, method, args);
    }

    private String generateDefaultKey(Method method, Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                sb.append(arg.toString()).append(":");
            }
        }
        return String.valueOf(sb.toString().hashCode());
    }
}
