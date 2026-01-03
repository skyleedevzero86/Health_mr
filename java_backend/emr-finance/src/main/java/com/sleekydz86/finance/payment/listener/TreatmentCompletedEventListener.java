package com.sleekydz86.finance.payment.listener;

import com.sleekydz86.core.event.domain.TreatmentCompletedEvent;
import com.sleekydz86.finance.payment.dto.PaymentRegisterRequest;
import com.sleekydz86.finance.payment.service.PaymentService;
import com.sleekydz86.finance.type.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TreatmentCompletedEventListener {

    private final PaymentService paymentService;

    @EventListener
    @Async
    public void handleTreatmentCompleted(TreatmentCompletedEvent event) {
        try {
            log.info("진료 완료 이벤트 수신: TreatmentId={}", event.treatmentId());

            PaymentRegisterRequest request = new PaymentRegisterRequest();
            request.setTreatmentId(event.treatmentId());
            request.setPaymentMethod(PaymentMethod.CASH);

            paymentService.registerPayment(request);

            log.info("결제 정보 자동 생성 완료: TreatmentId={}", event.treatmentId());
        } catch (Exception e) {
            log.error("결제 정보 자동 생성 실패: TreatmentId={}", event.treatmentId(), e);
        }
    }
}
