package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PatientRegisteredEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final Long patientNo;
    private final String patientName;
    private final String patientRrn;

    public PatientRegisteredEvent(Long patientNo, String patientName, String patientRrn) {
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
        return "PatientRegistered";
    }

    public Long getPatientNo() {
        return patientNo;
    }

    public String getPatientRrn() {
        return patientRrn;
    }

    public String getPatientName() {
        return patientName;
    }
}

