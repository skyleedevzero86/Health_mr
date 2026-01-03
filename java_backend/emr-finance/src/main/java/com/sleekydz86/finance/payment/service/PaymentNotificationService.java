package com.sleekydz86.finance.payment.service;

import com.sleekydz86.core.notification.service.NotificationService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationService {

    private final NotificationService notificationService;

    public void sendPaymentCompletedNotification(PaymentEntity payment) {
        try {
            PatientEntity patient = payment.getPatientEntity();
            if (patient == null) {
                log.warn("환자 정보가 없어 알림을 발송할 수 없습니다. PaymentId={}", payment.getPaymentId());
                return;
            }

            if (patient.getPatientEmail() != null && !patient.getPatientEmail().isEmpty()) {
                String subject = "결제 완료 안내";
                String message = String.format(
                        "안녕하세요 %s님,\n\n" +
                                "결제가 완료되었습니다.\n\n" +
                                "결제 정보:\n" +
                                "- 결제 금액: %,d원\n" +
                                "- 결제 수단: %s\n" +
                                "- 결제 일시: %s\n\n" +
                                "감사합니다.",
                        patient.getPatientName(),
                        payment.getPaymentCurrentMoney() != null ? payment.getPaymentCurrentMoney() : 0,
                        payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "",
                        payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : ""
                );

                notificationService.send(patient.getPatientEmail(), subject, message);
                log.info("결제 완료 알림 발송 성공: PaymentId={}, PatientEmail={}",
                        payment.getPaymentId(), patient.getPatientEmail());
            } else {
                log.debug("환자 이메일이 없어 알림을 발송하지 않습니다. PaymentId={}", payment.getPaymentId());
            }
        } catch (Exception e) {
            log.error("결제 완료 알림 발송 실패: PaymentId={}", payment.getPaymentId(), e);
        }
    }

    public void sendUnpaidNotification(PaymentEntity payment) {
        try {
            PatientEntity patient = payment.getPatientEntity();
            if (patient == null) {
                log.warn("환자 정보가 없어 알림을 발송할 수 없습니다. PaymentId={}", payment.getPaymentId());
                return;
            }

            if (patient.getPatientEmail() != null && !patient.getPatientEmail().isEmpty()) {
                String subject = "미납 안내";
                String message = String.format(
                        "안녕하세요 %s님,\n\n" +
                                "미납 금액이 있습니다.\n\n" +
                                "미납 정보:\n" +
                                "- 총 금액: %,d원\n" +
                                "- 미납 금액: %,d원\n" +
                                "- 결제 일시: %s\n\n" +
                                "빠른 시일 내에 결제 부탁드립니다.\n\n" +
                                "감사합니다.",
                        patient.getPatientName(),
                        payment.getPaymentTotalAmount() != null ? payment.getPaymentTotalAmount() : 0,
                        payment.getPaymentRemainMoney() != null ? payment.getPaymentRemainMoney() : 0,
                        payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : ""
                );

                notificationService.send(patient.getPatientEmail(), subject, message);
                log.info("미납 알림 발송 성공: PaymentId={}, PatientEmail={}",
                        payment.getPaymentId(), patient.getPatientEmail());
            } else {
                log.debug("환자 이메일이 없어 알림을 발송하지 않습니다. PaymentId={}", payment.getPaymentId());
            }
        } catch (Exception e) {
            log.error("미납 알림 발송 실패: PaymentId={}", payment.getPaymentId(), e);
        }
    }
}

