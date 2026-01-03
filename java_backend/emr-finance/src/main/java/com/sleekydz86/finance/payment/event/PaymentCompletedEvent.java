package com.sleekydz86.finance.payment.event;

import com.sleekydz86.core.event.domain.DomainEvent;
import java.util.UUID;
import java.time.LocalDateTime;

public class PaymentCompletedEvent implements DomainEvent {
    private final Long paymentId;
    private final Long treatmentId;
    private final Long patientNo;

    public PaymentCompletedEvent(Long paymentId, Long treatmentId, Long patientNo) {
        this.paymentId = paymentId;
        this.treatmentId = treatmentId;
        this.patientNo = patientNo;
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
        return "PaymentCompleted";
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getTreatmentId() {
        return treatmentId;
    }

    public Long getPatientNo() {
        return patientNo;
    }
}

