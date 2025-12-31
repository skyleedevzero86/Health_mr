package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserRegisteredEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final Long userId;
    private final String loginId;
    private final String role;

    public UserRegisteredEvent(Long userId, String loginId, String role) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.userId = userId;
        this.loginId = loginId;
        this.role = role;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getEventType() {
        return "UserRegistered";
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getRole() {
        return role;
    }
}

