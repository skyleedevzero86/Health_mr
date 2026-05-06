package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MedicalFeeCalculator {

    private final PaymentRepository paymentRepository;

    public long calculateMedicalFee(List<TreatmentEntity> treatments) {
        return treatments.stream()
                .mapToLong(t -> {
                    PaymentEntity payment = paymentRepository
                            .findByTreatmentEntity_TreatmentId(t.getTreatmentId())
                            .orElse(null);
                    return payment != null && payment.getPaymentTotalAmountValue() != null
                            ? payment.getPaymentTotalAmountValue()
                            : 0L;
                })
                .sum();
    }

    public long calculateBenefitFee(List<TreatmentEntity> treatments) {
        return treatments.stream()
                .mapToLong(t -> {
                    PaymentEntity payment = paymentRepository
                            .findByTreatmentEntity_TreatmentId(t.getTreatmentId())
                            .orElse(null);
                    return payment != null && payment.getPaymentInsuranceMoneyValue() != null
                            ? payment.getPaymentInsuranceMoneyValue()
                            : 0L;
                })
                .sum();
    }
}

