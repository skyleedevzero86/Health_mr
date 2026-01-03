package com.sleekydz86.finance.payment.listener;

import com.sleekydz86.finance.medicalfee.event.MedicalFeeCreatedEvent;
import com.sleekydz86.finance.medicalfee.event.MedicalFeeDeletedEvent;
import com.sleekydz86.finance.medicalfee.event.MedicalFeeUpdatedEvent;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.finance.payment.dto.PaymentCalculationResult;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.payment.service.PaymentCalculationService;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalFeeChangedEventListener {

    private final PaymentRepository paymentRepository;
    private final PaymentCalculationService paymentCalculationService;

    @EventListener
    @Async
    @Transactional
    public void handleMedicalFeeCreated(MedicalFeeCreatedEvent event) {
        try {
            log.info("진료비 생성 이벤트 수신: MedicalFeeId={}, TreatmentId={}",
                    event.getMedicalFeeId(), event.getTreatmentId());

            recalculatePayment(event.getTreatmentId());
        } catch (Exception e) {
            log.error("결제 금액 재계산 실패: TreatmentId={}", event.getTreatmentId(), e);
        }
    }

    @EventListener
    @Async
    @Transactional
    public void handleMedicalFeeUpdated(MedicalFeeUpdatedEvent event) {
        try {
            log.info("진료비 수정 이벤트 수신: MedicalFeeId={}, TreatmentId={}",
                    event.getMedicalFeeId(), event.getTreatmentId());

            recalculatePayment(event.getTreatmentId());
        } catch (Exception e) {
            log.error("결제 금액 재계산 실패: TreatmentId={}", event.getTreatmentId(), e);
        }
    }

    @EventListener
    @Async
    @Transactional
    public void handleMedicalFeeDeleted(MedicalFeeDeletedEvent event) {
        try {
            log.info("진료비 삭제 이벤트 수신: MedicalFeeId={}, TreatmentId={}",
                    event.getMedicalFeeId(), event.getTreatmentId());

            recalculatePayment(event.getTreatmentId());
        } catch (Exception e) {
            log.error("결제 금액 재계산 실패: TreatmentId={}", event.getTreatmentId(), e);
        }
    }

    private void recalculatePayment(Long treatmentId) {

        Optional<PaymentEntity> paymentOpt = paymentRepository.findByTreatmentEntity_TreatmentId(treatmentId);

        if (paymentOpt.isEmpty()) {
            log.debug("결제 정보가 없어 재계산을 건너뜁니다: TreatmentId={}", treatmentId);
            return;
        }

        PaymentEntity payment = paymentOpt.get();

        if (payment.getPaymentStatus() != PaymentStatus.UNPAID &&
                payment.getPaymentStatus() != PaymentStatus.PARTIAL) {
            log.debug("결제 상태가 UNPAID 또는 PARTIAL이 아니어서 재계산을 건너뜁니다: Status={}",
                    payment.getPaymentStatus());
            return;
        }

        PaymentCalculationResult calculationResult =
                paymentCalculationService.calculatePaymentAmount(
                        payment.getTreatmentEntity(),
                        payment.getPatientEntity()
                );

        Money totalAmount = Money.of(calculationResult.getTotalAmount());
        Money selfPay = Money.of(calculationResult.getSelfPay());
        Money insuranceMoney = Money.of(calculationResult.getInsuranceMoney());
        
        payment.initialize(totalAmount, selfPay, insuranceMoney);

        Money currentPaid = payment.getPaymentCurrentMoney() != null ? payment.getPaymentCurrentMoney() : Money.zero();
        Money newRemainMoney = selfPay.subtract(currentPaid);

        if (newRemainMoney.isLessThanOrEqual(Money.zero())) {
            payment.initialize(totalAmount, selfPay, insuranceMoney);
        } else {
            payment.initialize(totalAmount, selfPay, insuranceMoney);
            payment.partialPay(currentPaid);
        }

        paymentRepository.save(payment);

        log.info("결제 금액 재계산 완료: PaymentId={}, TreatmentId={}",
                payment.getPaymentId(), treatmentId);
    }
}

