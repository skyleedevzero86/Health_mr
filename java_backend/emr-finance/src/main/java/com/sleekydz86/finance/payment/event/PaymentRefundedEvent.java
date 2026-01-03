package com.sleekydz86.finance.payment.event;

import com.sleekydz86.core.event.domain.DomainEvent;
import java.util.UUID;
import java.time.LocalDateTime;

public class PaymentRefundedEvent implements DomainEvent {
    private final Long paymentId;
    private final Long treatmentId;
    private final Long refundAmount;

    public PaymentRefundedEvent(Long paymentId, Long treatmentId, Long refundAmount) {
        this.paymentId = paymentId;
        this.treatmentId = treatmentId;
        this.refundAmount = refundAmount;
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
        return "PaymentRefunded";
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getTreatmentId() {
        return treatmentId;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }
}

