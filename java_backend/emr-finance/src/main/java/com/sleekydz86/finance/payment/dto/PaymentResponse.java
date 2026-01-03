package com.sleekydz86.finance.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private Long treatmentId;
    private Long patientNo;
    private PaymentStatus paymentStatus;
    private Long paymentTotalAmount;
    private Long paymentSelfPay;
    private Long paymentInsuranceMoney;
    private Long paymentCurrentMoney;
    private Long paymentAmount;
    private Long paymentRemainMoney;
    private PaymentMethod paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;

    private String approvalNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalDate;

    private String cardCompany;

    public static PaymentResponse from(PaymentEntity entity) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(entity.getPaymentId());
        response.setTreatmentId(entity.getTreatmentEntity().getTreatmentId());
        if (entity.getPatientEntity() != null) {
            response.setPatientNo(entity.getPatientEntity().getPatientNo());
        }
        response.setPaymentStatus(entity.getPaymentStatus());
        response.setPaymentTotalAmount(entity.getPaymentTotalAmount());
        response.setPaymentSelfPay(entity.getPaymentSelfPay());
        response.setPaymentInsuranceMoney(entity.getPaymentInsuranceMoney());
        response.setPaymentCurrentMoney(entity.getPaymentCurrentMoney());
        response.setPaymentAmount(entity.getPaymentAmount());
        response.setPaymentRemainMoney(entity.getPaymentRemainMoney());
        response.setPaymentMethod(entity.getPaymentMethod());
        response.setPaymentDate(entity.getPaymentDate());
        response.setApprovalNumber(entity.getApprovalNumber());
        response.setApprovalDate(entity.getApprovalDate());
        response.setCardCompany(entity.getCardCompany());
        return response;
    }
}

