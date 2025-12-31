package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record PrescriptionCreatedEvent(
        Long prescriptionId,
        Long patientNo,
        String patientName,
        Long doctorId
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
        return "PrescriptionCreated";
    }
}
