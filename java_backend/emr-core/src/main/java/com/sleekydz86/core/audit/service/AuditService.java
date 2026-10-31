package com.sleekydz86.core.audit.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sleekydz86.core.audit.entity.AuditEntity;
import com.sleekydz86.core.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final Set<String> BLOCKED_KEYS = Set.of(
            "patientname", "patientrrn", "patienttel", "patientemail", "patientpassport",
            "rrn", "tel", "phone", "email", "passport", "username", "name"
    );
    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(Long userId, String actionType, String entityType, String entityId,
                         Object beforeData, Object afterData, String ipAddress, String userAgent) {
        logAudit(userId, actionType, entityType, entityId, beforeData, afterData, ipAddress, userAgent, "SUCCESS");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(Long userId, String actionType, String entityType, String entityId,
                         Object beforeData, Object afterData, String ipAddress, String userAgent, String outcome) {
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
                    .outcome(outcome)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditRepository.save(auditEntity);
            log.debug("감사 로그 기록 성공: {} - {} - {} - {}", actionType, entityType, entityId, outcome);
        } catch (Exception e) {
            log.error("감사 로그 기록 실패: {} - {} - {}", actionType, entityType, entityId, e);

        }
    }


    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(obj);
            return objectMapper.writeValueAsString(stripPersonalData(node));
        } catch (Exception e) {
            log.warn("JSON 변환 실패, 생략: {}", obj.getClass().getName(), e);
            return null;
        }
    }

    private JsonNode stripPersonalData(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();
            java.util.List<String> removed = new java.util.ArrayList<>();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (BLOCKED_KEYS.contains(fieldName.toLowerCase(Locale.ROOT))) {
                    removed.add(fieldName);
                } else {
                    objectNode.set(fieldName, stripPersonalData(objectNode.get(fieldName)));
                }
            }
            removed.forEach(objectNode::remove);
            return objectNode;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, stripPersonalData(arrayNode.get(i)));
            }
            return arrayNode;
        }
        return node;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSimpleAudit(Long userId, String actionType, String entityType, String entityId,
                               String ipAddress, String userAgent) {
        logAudit(userId, actionType, entityType, entityId, null, null, ipAddress, userAgent);
    }
}
