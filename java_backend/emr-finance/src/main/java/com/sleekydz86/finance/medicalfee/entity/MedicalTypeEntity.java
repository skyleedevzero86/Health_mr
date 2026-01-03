package com.sleekydz86.finance.medicalfee.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.finance.common.valueobject.Money;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "medical_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalTypeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_type_id", nullable = false)
    private Long medicalTypeId;

    @Column(name = "medical_type_code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "진료 유형 코드는 필수입니다.")
    private String medicalTypeCode;

    @Column(name = "medical_type_name", nullable = false, unique = true)
    @NotBlank(message = "진료 유형명은 필수입니다.")
    private String medicalTypeName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "medical_type_fee", nullable = false))
    private Money medicalTypeFee;

    @Column(name = "medical_type_description", columnDefinition = "TEXT")
    private String medicalTypeDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private MedicalTypeEntity(
            Long medicalTypeId,
            String medicalTypeCode,
            String medicalTypeName,
            Money medicalTypeFee,
            String medicalTypeDescription,
            Boolean isActive
    ) {
        validate(medicalTypeCode, medicalTypeName, medicalTypeFee);
        this.medicalTypeId = medicalTypeId;
        this.medicalTypeCode = medicalTypeCode;
        this.medicalTypeName = medicalTypeName;
        this.medicalTypeFee = medicalTypeFee;
        this.medicalTypeDescription = medicalTypeDescription;
        this.isActive = isActive != null ? isActive : true;
    }

    private void validate(String medicalTypeCode, String medicalTypeName, Money medicalTypeFee) {
        if (medicalTypeCode == null || medicalTypeCode.isBlank()) {
            throw new IllegalArgumentException("진료 유형 코드는 필수입니다.");
        }
        if (medicalTypeName == null || medicalTypeName.isBlank()) {
            throw new IllegalArgumentException("진료 유형명은 필수입니다.");
        }
        if (medicalTypeFee == null || medicalTypeFee.isZero()) {
            throw new IllegalArgumentException("진료 유형별 비용은 0보다 커야 합니다.");
        }
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateFee(Money newFee) {
        if (newFee == null || newFee.isZero()) {
            throw new IllegalArgumentException("진료 유형별 비용은 0보다 커야 합니다.");
        }
        this.medicalTypeFee = newFee;
    }

    public void updateInfo(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.medicalTypeName = name;
        }
        if (description != null) {
            this.medicalTypeDescription = description;
        }
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public Long getMedicalTypeFeeValue() {
        return medicalTypeFee != null ? medicalTypeFee.getValue() : null;
    }
}