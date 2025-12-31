package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserLoggedInEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final Long userId;
    private final String loginId;

    public UserLoggedInEvent(Long userId, String loginId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.userId = userId;
        this.loginId = loginId;
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
        return "UserLoggedIn";
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }
}

