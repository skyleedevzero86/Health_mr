package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PatientUpdatedEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final Long patientNo;
    private final String patientName;

    public PatientUpdatedEvent(Long patientNo, String patientName) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.patientNo = patientNo;
        this.patientName = patientName;
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
        return "PatientUpdated";
    }

    public Long getPatientNo() {
        return patientNo;
    }

    public String getPatientName() {
        return patientName;
    }
}

