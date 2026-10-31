package com.sleekydz86.core.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.audit.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Object beforeData = null;
        Long userId = resolveUserId();
        String ipAddress = null;
        String userAgent = null;

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getRemoteAddr();
            userAgent = request.getHeader("User-Agent");
        }

        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            beforeData = args[0];
        }

        String entityType = joinPoint.getSignature().getDeclaringType().getSimpleName();
        try {
            Object result = joinPoint.proceed();
            String entityId = extractEntityId(beforeData, result);
            scheduleSuccess(userId, auditLog.action().name(), entityType, entityId, beforeData, result, ipAddress, userAgent);
            return result;
        } catch (Throwable throwable) {
            if (userId != null) {
                auditService.logAudit(
                        userId,
                        auditLog.action().name(),
                        entityType,
                        extractEntityId(beforeData, null),
                        beforeData,
                        throwable.getMessage(),
                        ipAddress,
                        userAgent,
                        "FAIL"
                );
            }
            throw throwable;
        }
    }

    private void scheduleSuccess(Long userId, String actionType, String entityType, String entityId,
                                 Object beforeData, Object afterData, String ipAddress, String userAgent) {
        if (userId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    auditService.logAudit(userId, actionType, entityType, entityId, beforeData, afterData, ipAddress, userAgent, "SUCCESS");
                }
            });
            return;
        }
        auditService.logAudit(userId, actionType, entityType, entityId, beforeData, afterData, ipAddress, userAgent, "SUCCESS");
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            try {
                return Long.valueOf(authentication.getPrincipal().toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String extractEntityId(Object beforeData, Object afterData) {
        try {
            if (afterData != null) {
                var node = objectMapper.readTree(objectMapper.writeValueAsString(afterData));
                for (String field : new String[]{"id", "checkInId", "treatmentId", "paymentId", "prescriptionId"}) {
                    if (node.hasNonNull(field)) {
                        return node.get(field).asText();
                    }
                    if (node.has("data") && node.get("data").hasNonNull(field)) {
                        return node.get("data").get(field).asText();
                    }
                }
            }
            if (beforeData != null) {
                var node = objectMapper.readTree(objectMapper.writeValueAsString(beforeData));
                if (node.hasNonNull("id")) {
                    return node.get("id").asText();
                }
            }
        } catch (Exception e) {
            log.debug("엔티티 ID 추출 실패", e);
        }
        return "unknown";
    }
}
