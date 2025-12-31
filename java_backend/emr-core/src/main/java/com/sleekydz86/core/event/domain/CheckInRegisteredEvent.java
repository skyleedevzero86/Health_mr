package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckInRegisteredEvent(
        Long checkInId,
        Long patientNo,
        String patientName,
        Long userId,
        LocalDateTime checkInDate
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
        return "CheckInRegistered";
    }
}
