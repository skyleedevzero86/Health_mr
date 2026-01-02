package com.sleekydz86.emrclinical.prescription.notification;

import com.sleekydz86.core.notification.service.NotificationService;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionNotificationService {

    private final NotificationService notificationService;

    @Value("${notification.clinical.prescription.enabled:true}")
    private boolean notificationEnabled;

    @Value("${notification.clinical.prescription.on-create:true}")
    private boolean notifyOnCreate;

    public void sendPrescriptionCreatedNotification(PrescriptionEntity prescription) {
        if (!notificationEnabled || !notifyOnCreate) {
            return;
        }

        try {
            String patientEmail = prescription.getPatientEntity().getPatientEmailValue();
            if (patientEmail == null || patientEmail.isBlank()) {
                log.warn("환자 이메일이 없어 알림을 발송할 수 없습니다. PrescriptionId: {}", prescription.getPrescriptionId());
                return;
            }

            String subject = "처방전이 발급되었습니다";
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(String.format(
                    "안녕하세요 %s님,\n\n" +
                            "처방전이 발급되었습니다.\n\n" +
                            "처방 정보:\n" +
                            "- 처방 일시: %s\n" +
                            "- 처방 의사: %s\n" +
                            "- 처방 유형: %s\n" +
                            "- 처방 상태: %s\n\n",
                    prescription.getPatientEntity().getPatientName(),
                    prescription.getPrescriptionDate(),
                    prescription.getPrescriptionDoc().getName(),
                    prescription.getPrescriptionType(),
                    prescription.getPrescriptionStatus()));

            if (prescription.getPrescriptionItems() != null && !prescription.getPrescriptionItems().isEmpty()) {
                messageBuilder.append("처방 약물:\n");
                prescription.getPrescriptionItems().forEach(item -> {
                    messageBuilder.append(String.format(
                            "- %s (%s): %s, %s, %d일\n",
                            item.getDrugName(),
                            item.getDrugCode(),
                            item.getDosage(),
                            item.getDose(),
                            item.getDays()));
                });
                messageBuilder.append("\n");
            }

            messageBuilder.append("처방전을 조제받으시기 바랍니다.\n\n");
            messageBuilder.append("감사합니다.");

            notificationService.send(patientEmail, subject, messageBuilder.toString());
            log.info("처방 생성 알림 발송 완료: PrescriptionId={}, PatientEmail={}", prescription.getPrescriptionId(),
                    patientEmail);
        } catch (Exception e) {
            log.error("처방 생성 알림 발송 실패: PrescriptionId={}", prescription.getPrescriptionId(), e);
        }
    }
}
