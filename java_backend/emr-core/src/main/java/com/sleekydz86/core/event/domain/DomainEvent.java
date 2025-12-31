package com.sleekydz86.core.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {

    UUID getEventId();

    LocalDateTime getOccurredAt();

    String getEventType();

    default String getEventVersion() {
        return "1.0";
    }

    default boolean isCritical() {
        return false;
    }
}

