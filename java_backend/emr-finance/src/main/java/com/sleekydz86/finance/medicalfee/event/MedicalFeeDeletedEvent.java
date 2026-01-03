package com.sleekydz86.finance.medicalfee.event;

import com.sleekydz86.core.event.domain.DomainEvent;
import java.util.UUID;
import java.time.LocalDateTime;

public class MedicalFeeDeletedEvent implements DomainEvent {
    private final Long medicalFeeId;
    private final Long treatmentId;

    public MedicalFeeDeletedEvent(Long medicalFeeId, Long treatmentId) {
        this.medicalFeeId = medicalFeeId;
        this.treatmentId = treatmentId;
    }

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
        return "MedicalFeeDeleted";
    }

    public Long getMedicalFeeId() { 
        return medicalFeeId; 
    }
    
    public Long getTreatmentId() { 
        return treatmentId; 
    }
}

