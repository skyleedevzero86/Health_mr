package com.sleekydz86.finance.medicalfee.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.finance.common.valueobject.Money;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "medical_fee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalFeeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_fee_id", nullable = false)
    private Long medicalFeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_type_id", referencedColumnName = "medical_type_id", nullable = false)
    @NotNull(message = "진료 유형은 필수입니다.")
    private MedicalTypeEntity medicalTypeEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", referencedColumnName = "treatment_id")
    @NotNull(message = "진료 정보는 필수입니다.")
    private TreatmentEntity treatmentEntity;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "medical_fee_amount"))
    private Money medicalFeeAmount;

    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @Builder
    private MedicalFeeEntity(
            Long medicalFeeId,
            MedicalTypeEntity medicalTypeEntity,
            TreatmentEntity treatmentEntity,
            Money medicalFeeAmount,
            Integer quantity
    ) {
        validate(medicalTypeEntity, treatmentEntity, quantity);
        this.medicalFeeId = medicalFeeId;
        this.medicalTypeEntity = medicalTypeEntity;
        this.treatmentEntity = treatmentEntity;
        this.medicalFeeAmount = medicalFeeAmount != null ? medicalFeeAmount : calculateFeeFromType();
        this.quantity = quantity != null ? quantity : 1;
    }

    private void validate(MedicalTypeEntity medicalTypeEntity, TreatmentEntity treatmentEntity, Integer quantity) {
        if (medicalTypeEntity == null) {
            throw new IllegalArgumentException("진료 유형은 필수입니다.");
        }
        if (treatmentEntity == null) {
            throw new IllegalArgumentException("진료 정보는 필수입니다.");
        }
        if (quantity != null && quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다: " + quantity);
        }
    }

    private Money calculateFeeFromType() {
        if (medicalTypeEntity != null && medicalTypeEntity.getMedicalTypeFee() != null) {
            return Money.of(medicalTypeEntity.getMedicalTypeFeeValue());
        }
        return Money.zero();
    }

    public Money calculateTotalFee() {
        if (medicalFeeAmount == null || quantity == null) {
            return Money.zero();
        }
        return medicalFeeAmount.multiply(quantity);
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다: " + newQuantity);
        }
        this.quantity = newQuantity;
    }

    public void updateAmount(Money newAmount) {
        if (newAmount == null) {
            throw new IllegalArgumentException("금액은 필수입니다.");
        }
        this.medicalFeeAmount = newAmount;
    }

    public Long getMedicalFeeAmountValue() {
        return medicalFeeAmount != null ? medicalFeeAmount.getValue() : null;
    }
}

