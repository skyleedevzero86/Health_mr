package com.sleekydz86.emrclinical.prescription.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Prescription_Item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrescriptionItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_item_id", nullable = false)
    private Long prescriptionItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", referencedColumnName = "prescription_id", nullable = false)
    @NotNull(message = "처방 정보는 필수입니다.")
    private PrescriptionEntity prescriptionEntity;

    @Column(name = "drug_code", nullable = false, length = 50)
    @NotBlank(message = "약물 코드는 필수입니다.")
    @Size(max = 50, message = "약물 코드는 최대 50자까지 가능합니다.")
    private String drugCode;

    @Column(name = "drug_name", nullable = false, length = 200)
    @NotBlank(message = "약물명은 필수입니다.")
    @Size(max = 200, message = "약물명은 최대 200자까지 가능합니다.")
    private String drugName;

    @Column(name = "dosage", nullable = false, length = 500)
    @NotBlank(message = "용법은 필수입니다.")
    @Size(max = 500, message = "용법은 최대 500자까지 가능합니다.")
    private String dosage;

    @Column(name = "dose", nullable = false, length = 100)
    @NotBlank(message = "용량은 필수입니다.")
    @Size(max = 100, message = "용량은 최대 100자까지 가능합니다.")
    private String dose;

    @Column(name = "frequency", nullable = false)
    @NotNull(message = "횟수는 필수입니다.")
    @Min(value = 1, message = "횟수는 최소 1회 이상이어야 합니다.")
    @Max(value = 10, message = "횟수는 최대 10회까지 가능합니다.")
    private Integer frequency;

    @Column(name = "days", nullable = false)
    @NotNull(message = "일수는 필수입니다.")
    @Min(value = 1, message = "일수는 최소 1일 이상이어야 합니다.")
    @Max(value = 365, message = "일수는 최대 365일까지 가능합니다.")
    private Integer days;

    @Column(name = "total_quantity", nullable = false)
    @NotNull(message = "총 수량은 필수입니다.")
    @Min(value = 1, message = "총 수량은 최소 1개 이상이어야 합니다.")
    private Integer totalQuantity;

    @Column(name = "unit", nullable = false, length = 20)
    @NotBlank(message = "단위는 필수입니다.")
    @Size(max = 20, message = "단위는 최대 20자까지 가능합니다.")
    private String unit;

    @Column(name = "special_note", columnDefinition = "TEXT")
    private String specialNote;

    @Builder
    private PrescriptionItemEntity(
            Long prescriptionItemId,
            PrescriptionEntity prescriptionEntity,
            String drugCode,
            String drugName,
            String dosage,
            String dose,
            Integer frequency,
            Integer days,
            Integer totalQuantity,
            String unit,
            String specialNote
    ) {
        validate(drugCode, drugName, dosage, dose, frequency, days, totalQuantity, unit);
        this.prescriptionItemId = prescriptionItemId;
        this.prescriptionEntity = prescriptionEntity;
        this.drugCode = drugCode;
        this.drugName = drugName;
        this.dosage = dosage;
        this.dose = dose;
        this.frequency = frequency;
        this.days = days;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
        this.specialNote = specialNote;
    }

    private void validate(String drugCode, String drugName, String dosage, String dose,
                          Integer frequency, Integer days, Integer totalQuantity, String unit) {
        if (drugCode == null || drugCode.isBlank()) {
            throw new IllegalArgumentException("약물 코드는 필수입니다.");
        }
        if (drugName == null || drugName.isBlank()) {
            throw new IllegalArgumentException("약물명은 필수입니다.");
        }
        if (dosage == null || dosage.isBlank()) {
            throw new IllegalArgumentException("용법은 필수입니다.");
        }
        if (dose == null || dose.isBlank()) {
            throw new IllegalArgumentException("용량은 필수입니다.");
        }
        if (frequency == null || frequency < 1 || frequency > 10) {
            throw new IllegalArgumentException("횟수는 1회 이상 10회 이하여야 합니다: " + frequency);
        }
        if (days == null || days < 1 || days > 365) {
            throw new IllegalArgumentException("일수는 1일 이상 365일 이하여야 합니다: " + days);
        }
        if (totalQuantity == null || totalQuantity < 1) {
            throw new IllegalArgumentException("총 수량은 1개 이상이어야 합니다: " + totalQuantity);
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("단위는 필수입니다.");
        }
    }

    public void calculateTotalQuantity() {
        if (this.frequency != null && this.days != null) {
            this.totalQuantity = this.frequency * this.days;
        }
    }

    public void updateInfo(String dosage, String dose, Integer frequency, Integer days, String specialNote) {
        if (dosage != null && !dosage.isBlank()) {
            this.dosage = dosage;
        }
        if (dose != null && !dose.isBlank()) {
            this.dose = dose;
        }
        if (frequency != null) {
            if (frequency < 1 || frequency > 10) {
                throw new IllegalArgumentException("횟수는 1회 이상 10회 이하여야 합니다: " + frequency);
            }
            this.frequency = frequency;
            calculateTotalQuantity(); // 총 수량 재계산
        }
        if (days != null) {
            if (days < 1 || days > 365) {
                throw new IllegalArgumentException("일수는 1일 이상 365일 이하여야 합니다: " + days);
            }
            this.days = days;
            calculateTotalQuantity();
        }
        if (specialNote != null) {
            this.specialNote = specialNote;
        }
    }
}

