package com.sleekydz86.emrclinical.treatment.notification;

import com.sleekydz86.core.notification.service.NotificationService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentNotificationService {

    private final NotificationService notificationService;

    @Value("${notification.clinical.treatment.enabled:true}")
    private boolean notificationEnabled;

    @Value("${notification.clinical.treatment.on-complete:true}")
    private boolean notifyOnComplete;

    public void sendTreatmentCompletedNotification(TreatmentEntity treatment) {
        if (!notificationEnabled || !notifyOnComplete) {
            return;
        }

        try {

            String patientEmail = null;
            if (treatment.getCheckInEntity() != null && treatment.getCheckInEntity().getPatientEntity() != null) {
                patientEmail = treatment.getCheckInEntity().getPatientEntity().getPatientEmailValue();
            }

            if (patientEmail == null || patientEmail.isBlank()) {
                log.warn("환자 이메일이 없어 알림을 발송할 수 없습니다. TreatmentId: {}", treatment.getTreatmentId());
                return;
            }

            String patientName = treatment.getCheckInEntity().getPatientEntity().getPatientName();
            String subject = "진료가 완료되었습니다";
            String message = String.format(
                    "안녕하세요 %s님,\n\n" +
                            "진료가 완료되었습니다.\n\n" +
                            "진료 정보:\n" +
                            "- 진료 일시: %s\n" +
                            "- 진료 유형: %s\n" +
                            "- 담당 의사: %s\n" +
                            "- 진료과: %s\n\n" +
                            "처방전이 필요하시면 처방전을 발급받으시기 바랍니다.\n\n" +
                            "감사합니다.",
                    patientName,
                    treatment.getTreatmentDate(),
                    treatment.getTreatmentType(),
                    treatment.getTreatmentDoc().getName(),
                    treatment.getDepartmentEntity() != null ? treatment.getDepartmentEntity().getName() : "미지정"
            );

            notificationService.send(patientEmail, subject, message);
            log.info("진료 완료 알림 발송 완료: TreatmentId={}, PatientEmail={}", treatment.getTreatmentId(), patientEmail);
        } catch (Exception e) {
            log.error("진료 완료 알림 발송 실패: TreatmentId={}", treatment.getTreatmentId(), e);
        }
    }
}

