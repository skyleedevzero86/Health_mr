package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class DepartmentUpdatedEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final Long departmentId;
    private final String code;
    private final String name;

    public DepartmentUpdatedEvent(Long departmentId, String code, String name) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.departmentId = departmentId;
        this.code = code;
        this.name = name;
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
        return "DepartmentUpdated";
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}

