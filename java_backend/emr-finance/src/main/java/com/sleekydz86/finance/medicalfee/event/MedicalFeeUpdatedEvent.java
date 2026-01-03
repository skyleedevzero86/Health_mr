package com.sleekydz86.finance.medicalfee.event;

import com.sleekydz86.core.event.domain.DomainEvent;
import java.util.UUID;
import java.time.LocalDateTime;

public class MedicalFeeUpdatedEvent implements DomainEvent {
    private final Long medicalFeeId;
    private final Long treatmentId;
    private final Long medicalTypeId;

    public MedicalFeeUpdatedEvent(Long medicalFeeId, Long treatmentId, Long medicalTypeId) {
        this.medicalFeeId = medicalFeeId;
        this.treatmentId = treatmentId;
        this.medicalTypeId = medicalTypeId;
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
        return "MedicalFeeUpdated";
    }

    public Long getMedicalFeeId() { 
        return medicalFeeId; 
    }
    
    public Long getTreatmentId() { 
        return treatmentId; 
    }
    
    public Long getMedicalTypeId() { 
        return medicalTypeId; 
    }
}

