package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import org.springframework.stereotype.Component;

@Component
public class MedicalFeeNotificationMessageBuilder {

    public String buildSubject() {
        return "진료비 안내";
    }

    public String buildMessage(PatientEntity patient, MedicalFeeEntity medicalFee) {
        Long totalAmount = calculateTotalAmount(medicalFee);
        String medicalTypeName = getMedicalTypeName(medicalFee);
        Long medicalFeeAmount = medicalFee.getMedicalFeeAmount() != null ? medicalFee.getMedicalFeeAmount() : 0;
        Integer quantity = medicalFee.getQuantity() != null ? medicalFee.getQuantity() : 1;

        return String.format(
                "안녕하세요 %s님,\n\n" +
                        "진료비가 등록되었습니다.\n\n" +
                        "진료비 정보:\n" +
                        "- 진료 유형: %s\n" +
                        "- 진료비 금액: %,d원\n" +
                        "- 수량: %d\n" +
                        "- 총 금액: %,d원\n\n" +
                        "결제는 진료 완료 후 가능합니다.\n\n" +
                        "감사합니다.",
                patient.getPatientName(),
                medicalTypeName,
                medicalFeeAmount,
                quantity,
                totalAmount);
    }

    private Long calculateTotalAmount(MedicalFeeEntity medicalFee) {
        Long amount = medicalFee.getMedicalFeeAmount() != null ? medicalFee.getMedicalFeeAmount() : 0L;
        Integer quantity = medicalFee.getQuantity() != null ? medicalFee.getQuantity() : 1;
        return amount * quantity;
    }

    private String getMedicalTypeName(MedicalFeeEntity medicalFee) {
        if (medicalFee.getMedicalTypeEntity() != null) {
            return medicalFee.getMedicalTypeEntity().getMedicalTypeName();
        }
        return "";
    }
}

