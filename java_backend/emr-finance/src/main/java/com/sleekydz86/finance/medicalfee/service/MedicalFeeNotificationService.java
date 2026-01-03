package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.core.notification.service.NotificationService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalFeeNotificationService {

    private final NotificationService notificationService;
    private final MedicalFeeNotificationMessageBuilder messageBuilder;

    public void sendMedicalFeeRegisteredNotification(MedicalFeeEntity medicalFee) {
        try {
            PatientEntity patient = extractPatient(medicalFee);

            if (patient == null) {
                log.warn("환자 정보가 없어 알림을 발송할 수 없습니다. MedicalFeeId={}", medicalFee.getMedicalFeeId());
                return;
            }

            String patientEmail = patient.getPatientEmailValue();
            if (patientEmail != null && !patientEmail.isEmpty()) {
                String subject = messageBuilder.buildSubject();
                String message = messageBuilder.buildMessage(patient, medicalFee);

                notificationService.send(patientEmail, subject, message);
                log.info("진료비 등록 알림 발송 성공: MedicalFeeId={}, PatientEmail={}",
                        medicalFee.getMedicalFeeId(), patientEmail);
            } else {
                log.debug("환자 이메일이 없어 알림을 발송하지 않습니다. MedicalFeeId={}", medicalFee.getMedicalFeeId());
            }
        } catch (Exception e) {
            log.error("진료비 등록 알림 발송 실패: MedicalFeeId={}", medicalFee.getMedicalFeeId(), e);
        }
    }

    private PatientEntity extractPatient(MedicalFeeEntity medicalFee) {
        if (medicalFee.getTreatmentEntity() != null &&
                medicalFee.getTreatmentEntity().getCheckInEntity() != null &&
                medicalFee.getTreatmentEntity().getCheckInEntity().getPatientEntity() != null) {
            return medicalFee.getTreatmentEntity().getCheckInEntity().getPatientEntity();
        }
        return null;
    }
}
