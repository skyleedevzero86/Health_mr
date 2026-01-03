package com.sleekydz86.finance.payment.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "Payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", referencedColumnName = "treatment_id", nullable = false)
    @NotNull(message = "진료 정보는 필수입니다.")
    private TreatmentEntity treatmentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no")
    private PatientEntity patientEntity;

    @Column(name = "payment_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentStatus paymentStatus;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_total_amount"))
    private Money paymentTotalAmount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_self_pay"))
    private Money paymentSelfPay;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_insurance_money"))
    private Money paymentInsuranceMoney;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_current_money"))
    private Money paymentCurrentMoney;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_amount"))
    private Money paymentAmount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_remain_money"))
    private Money paymentRemainMoney;

    @Column(name = "payment_method", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "refund_amount"))
    private Money refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_method", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethod refundMethod;

    @Column(name = "approval_number", length = 50)
    private String approvalNumber;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "card_company", length = 50)
    private String cardCompany;

    @Builder
    private PaymentEntity(
            Long paymentId,
            TreatmentEntity treatmentEntity,
            PatientEntity patientEntity,
            PaymentStatus paymentStatus,
            Money paymentTotalAmount,
            Money paymentSelfPay,
            Money paymentInsuranceMoney,
            Money paymentCurrentMoney,
            Money paymentAmount,
            Money paymentRemainMoney,
            PaymentMethod paymentMethod,
            LocalDateTime paymentDate,
            String cancelReason,
            Money refundAmount,
            LocalDateTime refundDate,
            PaymentMethod refundMethod,
            String approvalNumber,
            LocalDateTime approvalDate,
            String cardCompany
    ) {
        this.paymentId = paymentId;
        this.treatmentEntity = treatmentEntity;
        this.patientEntity = patientEntity;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.UNPAID;
        this.paymentTotalAmount = paymentTotalAmount;
        this.paymentSelfPay = paymentSelfPay;
        this.paymentInsuranceMoney = paymentInsuranceMoney;
        this.paymentCurrentMoney = paymentCurrentMoney != null ? paymentCurrentMoney : Money.zero();
        this.paymentAmount = paymentAmount;
        this.paymentRemainMoney = paymentRemainMoney;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.cancelReason = cancelReason;
        this.refundAmount = refundAmount;
        this.refundDate = refundDate;
        this.refundMethod = refundMethod;
        this.approvalNumber = approvalNumber;
        this.approvalDate = approvalDate;
        this.cardCompany = cardCompany;
    }

    public void initialize(Money totalAmount, Money selfPay, Money insuranceMoney) {
        if (totalAmount == null || selfPay == null || insuranceMoney == null) {
            throw new IllegalArgumentException("금액 정보는 모두 필수입니다.");
        }
        if (!totalAmount.equals(selfPay.add(insuranceMoney))) {
            throw new IllegalArgumentException("총 금액은 본인 부담금과 보험 지원금의 합과 일치해야 합니다.");
        }

        this.paymentTotalAmount = totalAmount;
        this.paymentSelfPay = selfPay;
        this.paymentInsuranceMoney = insuranceMoney;
        this.paymentCurrentMoney = Money.zero();
        this.paymentRemainMoney = totalAmount;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public void complete(PaymentMethod method, String approvalNumber, String cardCompany) {
        validateCanComplete();

        this.paymentStatus = PaymentStatus.PAID;
        this.paymentMethod = method;
        this.paymentDate = LocalDateTime.now();
        this.approvalNumber = approvalNumber;
        this.approvalDate = LocalDateTime.now();
        this.cardCompany = cardCompany;
        this.paymentCurrentMoney = this.paymentTotalAmount;
        this.paymentRemainMoney = Money.zero();
    }

    public void updatePaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("결제 수단은 필수입니다.");
        }
        this.paymentMethod = method;
    }

    public void partialPay(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        if (this.paymentRemainMoney == null || amount.isGreaterThanOrEqual(this.paymentRemainMoney)) {
            throw new IllegalArgumentException("결제 금액이 남은 금액보다 크거나 같습니다.");
        }

        this.paymentCurrentMoney = this.paymentCurrentMoney.add(amount);
        this.paymentRemainMoney = this.paymentRemainMoney.subtract(amount);
        this.paymentDate = LocalDateTime.now();

        if (this.paymentRemainMoney.isZero()) {
            this.paymentStatus = PaymentStatus.PAID;
        } else {
            this.paymentStatus = PaymentStatus.PARTIAL;
        }
    }

    public void cancel(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("취소 사유는 필수입니다.");
        }
        if (this.paymentStatus == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("이미 환불된 결제는 취소할 수 없습니다.");
        }

        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public void refund(Money amount, PaymentMethod method) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("환불 금액은 0보다 커야 합니다.");
        }
        if (this.paymentCurrentMoney == null || amount.isGreaterThanOrEqual(this.paymentCurrentMoney)) {
            throw new IllegalArgumentException("환불 금액이 결제된 금액보다 크거나 같습니다.");
        }
        if (this.paymentStatus != PaymentStatus.PAID && this.paymentStatus != PaymentStatus.PARTIAL) {
            throw new IllegalStateException("결제 완료 또는 부분 결제 상태에서만 환불할 수 있습니다.");
        }

        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundAmount = amount;
        this.refundMethod = method;
        this.refundDate = LocalDateTime.now();
        this.paymentCurrentMoney = this.paymentCurrentMoney.subtract(amount);
        this.paymentRemainMoney = this.paymentRemainMoney.add(amount);
    }

    private void validateCanComplete() {
        if (this.paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("이미 결제 완료된 상태입니다.");
        }
        if (this.paymentStatus == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("취소된 결제는 완료할 수 없습니다.");
        }
        if (this.paymentStatus == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("환불된 결제는 완료할 수 없습니다.");
        }
    }

    public boolean isPaid() {
        return this.paymentStatus == PaymentStatus.PAID;
    }


    public boolean isPartial() {
        return this.paymentStatus == PaymentStatus.PARTIAL;
    }


    public boolean isUnpaid() {
        return this.paymentStatus == PaymentStatus.UNPAID;
    }

    public boolean isRefunded() {
        return this.paymentStatus == PaymentStatus.REFUNDED;
    }

    public Long getPaymentTotalAmountValue() {
        return paymentTotalAmount != null ? paymentTotalAmount.getValue() : null;
    }

    public Long getPaymentSelfPayValue() {
        return paymentSelfPay != null ? paymentSelfPay.getValue() : null;
    }

    public Long getPaymentInsuranceMoneyValue() {
        return paymentInsuranceMoney != null ? paymentInsuranceMoney.getValue() : null;
    }

    public Long getPaymentCurrentMoneyValue() {
        return paymentCurrentMoney != null ? paymentCurrentMoney.getValue() : null;
    }

    public Long getPaymentAmountValue() {
        return paymentAmount != null ? paymentAmount.getValue() : null;
    }

    public Long getPaymentRemainMoneyValue() {
        return paymentRemainMoney != null ? paymentRemainMoney.getValue() : null;
    }

    public Long getRefundAmountValue() {
        return refundAmount != null ? refundAmount.getValue() : null;
    }
}