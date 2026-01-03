package com.sleekydz86.finance.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.type.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 결제 상세 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse extends PaymentResponse {

    private String cancelReason;
    private Long refundAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundDate;

    private PaymentMethod refundMethod;

    public static PaymentDetailResponse from(PaymentEntity entity) {
        PaymentDetailResponse response = new PaymentDetailResponse();

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

        response.setCancelReason(entity.getCancelReason());
        response.setRefundAmount(entity.getRefundAmount());
        response.setRefundDate(entity.getRefundDate());
        response.setRefundMethod(entity.getRefundMethod());

        return response;
    }
}
