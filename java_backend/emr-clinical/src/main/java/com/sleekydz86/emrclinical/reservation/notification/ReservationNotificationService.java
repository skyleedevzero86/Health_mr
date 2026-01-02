package com.sleekydz86.emrclinical.reservation.notification;

import com.sleekydz86.core.notification.service.NotificationService;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationNotificationService {

    private final NotificationService notificationService;

    @Value("${notification.clinical.reservation.enabled:true}")
    private boolean notificationEnabled;

    @Value("${notification.clinical.reservation.on-register:true}")
    private boolean notifyOnRegister;

    @Value("${notification.clinical.reservation.on-update:true}")
    private boolean notifyOnUpdate;

    @Value("${notification.clinical.reservation.on-cancel:true}")
    private boolean notifyOnCancel;

    public void sendReservationRegisteredNotification(ReservationEntity reservation) {
        if (!notificationEnabled || !notifyOnRegister) {
            return;
        }

        try {
            String patientEmail = reservation.getPatientEntity().getPatientEmailValue();
            if (patientEmail == null || patientEmail.isBlank()) {
                log.warn("환자 이메일이 없어 알림을 발송할 수 없습니다. ReservationId: {}", reservation.getReservationId());
                return;
            }

            String subject = "예약이 등록되었습니다";
            String message = String.format(
                    "안녕하세요 %s님,\n\n" +
                            "예약이 성공적으로 등록되었습니다.\n\n" +
                            "예약 정보:\n" +
                            "- 예약 일시: %s\n" +
                            "- 예약 상태: %s\n\n" +
                            "예약 일시에 방문해 주시기 바랍니다.\n\n" +
                            "감사합니다.",
                    reservation.getPatientEntity().getPatientName(),
                    reservation.getReservationDate(),
                    reservation.getReservationStatus());

            notificationService.send(patientEmail, subject, message);
            log.info("예약 등록 알림 발송 완료: ReservationId={}, PatientEmail={}", reservation.getReservationId(), patientEmail);
        } catch (Exception e) {
            log.error("예약 등록 알림 발송 실패: ReservationId={}", reservation.getReservationId(), e);
        }
    }

    public void sendReservationUpdatedNotification(ReservationEntity reservation) {
        if (!notificationEnabled || !notifyOnUpdate) {
            return;
        }

        try {
            String patientEmail = reservation.getPatientEntity().getPatientEmailValue();
            if (patientEmail == null || patientEmail.isBlank()) {
                log.warn("환자 이메일이 없어 알림을 발송할 수 없습니다. ReservationId: {}", reservation.getReservationId());
                return;
            }

            String subject = "예약이 변경되었습니다";
            String message = String.format(
                    "안녕하세요 %s님,\n\n" +
                            "예약 정보가 변경되었습니다.\n\n" +
                            "변경된 예약 정보:\n" +
                            "- 예약 일시: %s\n" +
                            "- 예약 상태: %s\n" +
                            "- 변경 사유: %s\n\n" +
                            "변경된 예약 일시에 방문해 주시기 바랍니다.\n\n" +
                            "감사합니다.",
                    reservation.getPatientEntity().getPatientName(),
                    reservation.getReservationDate(),
                    reservation.getReservationStatus(),
                    reservation.getReservationChangeCause() != null ? reservation.getReservationChangeCause()
                            : "변경 사유 없음");

            notificationService.send(patientEmail, subject, message);
            log.info("예약 변경 알림 발송 완료: ReservationId={}, PatientEmail={}", reservation.getReservationId(), patientEmail);
        } catch (Exception e) {
            log.error("예약 변경 알림 발송 실패: ReservationId={}", reservation.getReservationId(), e);
        }
    }

    public void sendReservationCancelledNotification(ReservationEntity reservation, String cancelReason) {
        if (!notificationEnabled || !notifyOnCancel) {
            return;
        }

        try {
            String patientEmail = reservation.getPatientEntity().getPatientEmailValue();
            if (patientEmail == null || patientEmail.isBlank()) {
                log.warn("환자 이메일이 없어 알림을 발송할 수 없습니다. ReservationId: {}", reservation.getReservationId());
                return;
            }

            String subject = "예약이 취소되었습니다";
            String message = String.format(
                    "안녕하세요 %s님,\n\n" +
                            "예약이 취소되었습니다.\n\n" +
                            "취소된 예약 정보:\n" +
                            "- 예약 일시: %s\n" +
                            "- 취소 사유: %s\n\n" +
                            "추가 예약이 필요하시면 다시 예약해 주시기 바랍니다.\n\n" +
                            "감사합니다.",
                    reservation.getPatientEntity().getPatientName(),
                    reservation.getReservationDate(),
                    cancelReason != null ? cancelReason : "취소 사유 없음");

            notificationService.send(patientEmail, subject, message);
            log.info("예약 취소 알림 발송 완료: ReservationId={}, PatientEmail={}", reservation.getReservationId(), patientEmail);
        } catch (Exception e) {
            log.error("예약 취소 알림 발송 실패: ReservationId={}", reservation.getReservationId(), e);
        }
    }
}