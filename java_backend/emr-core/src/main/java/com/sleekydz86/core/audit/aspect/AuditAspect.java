package com.sleekydz86.core.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.core.audit.annotation.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Object beforeData = null;
        Object afterData = null;
        Long userId = null;
        String ipAddress = null;
        String userAgent = null;

        try {

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
                userAgent = request.getHeader("User-Agent");
            }

            // 실제 구현 시 UserEntity 타입 기능 완성처리필요
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                // UserEntity는 실제 도메인 모듈에서 확인
                //  Object의 id 필드를 리플렉션으로 추출하기
                if (arg != null) {
                    try {
                        java.lang.reflect.Method getIdMethod = arg.getClass().getMethod("getId");
                        Object id = getIdMethod.invoke(arg);
                        if (id instanceof Long) {
                            userId = (Long) id;
                            break;
                        }
                    } catch (Exception e) {
                        // 무시 하는 기능만들기
                    }
                }
            }

            if (args.length > 0) {
                beforeData = args[0];
            }

            Object result = joinPoint.proceed();

            afterData = result;

            String entityType = joinPoint.getSignature().getDeclaringTypeName();
            String entityId = extractEntityId(beforeData, afterData);

            if (userId != null) {
                auditService.logAudit(
                        userId,
                        auditLog.action().name(),
                        entityType,
                        entityId,
                        beforeData,
                        afterData,
                        ipAddress,
                        userAgent
                );
            }

            return result;
        } catch (Exception e) {
            log.error("감사 로그 처리 중 오류 발생", e);
            throw e;
        }
    }

    private String extractEntityId(Object beforeData, Object afterData) {
        try {
            if (beforeData != null) {
                return objectMapper.readTree(objectMapper.writeValueAsString(beforeData))
                        .get("id").asText();
            }
            if (afterData != null) {
                return objectMapper.readTree(objectMapper.writeValueAsString(afterData))
                        .get("id").asText();
            }
        } catch (Exception e) {
            log.debug("엔티티 ID 추출 실패", e);
        }
        return "unknown";
    }
}

