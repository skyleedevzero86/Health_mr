package com.sleekydz86.core.audit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String actionType;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false, length = 100)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String beforeData;

    @Column(columnDefinition = "TEXT")
    private String afterData;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private AuditEntity(
            Long id,
            Long userId,
            String actionType,
            String entityType,
            String entityId,
            String beforeData,
            String afterData,
            String ipAddress,
            String userAgent,
            LocalDateTime createdAt
    ) {
        validate(userId, actionType, entityType, entityId);
        this.id = id;
        this.userId = userId;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }


    private void validate(Long userId, String actionType, String entityType, String entityId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (actionType == null || actionType.isBlank()) {
            throw new IllegalArgumentException("액션 타입은 필수입니다.");
        }
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("엔티티 타입은 필수입니다.");
        }
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("엔티티 ID는 필수입니다.");
        }
    }


    public boolean hasChanges() {
        return beforeData != null && afterData != null && !beforeData.equals(afterData);
    }

    public boolean isCreateAction() {
        return "CREATE".equalsIgnoreCase(actionType);
    }

    public boolean isUpdateAction() {
        return "UPDATE".equalsIgnoreCase(actionType);
    }

    public boolean isDeleteAction() {
        return "DELETE".equalsIgnoreCase(actionType);
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

