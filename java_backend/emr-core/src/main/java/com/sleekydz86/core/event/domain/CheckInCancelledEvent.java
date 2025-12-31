package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckInCancelledEvent(
        Long checkInId,
        Long patientNo,
        String cancelReason
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
        return "CheckInCancelled";
    }
}