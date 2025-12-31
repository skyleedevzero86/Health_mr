package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckInCompletedEvent(
        Long checkInId,
        Long patientNo
) implements DomainEvent {
    @Override
    public UUID getEventId() {
        return UUID.randomUUID();
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return LocalDateTime.now();
    }

    @Override
    public String getEventType() {
        return "CheckInCompleted";
    }
}