package com.sleekydz86.core.audit.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.core.audit.entity.AuditEntity;
import com.sleekydz86.core.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logAudit(Long userId, String actionType, String entityType, String entityId,
                         Object beforeData, Object afterData, String ipAddress, String userAgent) {
        try {
            AuditEntity auditEntity = AuditEntity.builder()
                    .userId(userId)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .beforeData(toJson(beforeData))
                    .afterData(toJson(afterData))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditRepository.save(auditEntity);
            log.debug("감사 로그 기록 성공: {} - {} - {}", actionType, entityType, entityId);
        } catch (Exception e) {
            log.error("감사 로그 기록 실패: {} - {} - {}", actionType, entityType, entityId, e);

        }
    }


    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON 변환 실패, toString() 사용: {}", obj.getClass().getName(), e);
            return obj.toString();
        }
    }

    @Transactional
    public void logSimpleAudit(Long userId, String actionType, String entityType, String entityId,
                               String ipAddress, String userAgent) {
        logAudit(userId, actionType, entityType, entityId, null, null, ipAddress, userAgent);
    }
}
