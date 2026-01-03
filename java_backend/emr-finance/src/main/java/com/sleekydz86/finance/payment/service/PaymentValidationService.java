package com.sleekydz86.finance.payment.service;

import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.ValidationException;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    public void validatePaymentAmount(Long paymentAmount, Long totalAmount) {
        if (paymentAmount == null || paymentAmount < 0) {
            throw new ValidationException("결제 금액은 0 이상이어야 합니다.");
        }

        if (paymentAmount > totalAmount) {
            throw new ValidationException("결제 금액은 총 금액을 초과할 수 없습니다.");
        }
    }

    public void validatePaymentStatus(PaymentEntity payment, PaymentStatus... allowedStatuses) {
        PaymentStatus currentStatus = payment.getPaymentStatus();

        for (PaymentStatus allowedStatus : allowedStatuses) {
            if (currentStatus == allowedStatus) {
                return;
            }
        }

        throw new BusinessException("결제 상태가 올바르지 않습니다. 현재 상태: " + currentStatus);
    }

    public void validateTreatmentStatus(TreatmentEntity treatment) {
        if (treatment.getTreatmentStatus() != TreatmentStatus.COMPLETED) {
            throw new BusinessException("진료 완료된 경우에만 결제할 수 있습니다. 현재 상태: " + treatment.getTreatmentStatus());
        }
    }

    public void validateNotPaid(PaymentEntity payment) {
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException("완납된 결제는 수정할 수 없습니다.");
        }
    }

    public void validateCanRefund(PaymentEntity payment) {
        if (payment.getPaymentStatus() != PaymentStatus.PAID &&
                payment.getPaymentStatus() != PaymentStatus.PARTIAL) {
            throw new BusinessException("결제 완료 또는 부분 결제된 경우에만 환불할 수 있습니다.");
        }
    }

    public void validateRefundAmount(Long refundAmount, Long paidAmount) {
        if (refundAmount == null || refundAmount < 0) {
            throw new ValidationException("환불 금액은 0 이상이어야 합니다.");
        }

        if (refundAmount > paidAmount) {
            throw new ValidationException("환불 금액은 결제 금액을 초과할 수 없습니다.");
        }
    }
}

