package com.sleekydz86.core.clinical;

public interface TreatmentPaymentGuard {

    boolean hasCollectedPayment(Long treatmentId);
}
