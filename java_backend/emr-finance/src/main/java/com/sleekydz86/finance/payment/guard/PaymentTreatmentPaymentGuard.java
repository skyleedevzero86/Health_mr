package com.sleekydz86.finance.payment.guard;

import com.sleekydz86.core.clinical.TreatmentPaymentGuard;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentTreatmentPaymentGuard implements TreatmentPaymentGuard {

    private final PaymentRepository paymentRepository;

    @Override
    public boolean hasCollectedPayment(Long treatmentId) {
        return paymentRepository.findByTreatmentEntity_TreatmentId(treatmentId)
                .map(PaymentEntity::getPaymentStatus)
                .map(status -> status == PaymentStatus.PAID || status == PaymentStatus.PARTIAL)
                .orElse(false);
    }
}
