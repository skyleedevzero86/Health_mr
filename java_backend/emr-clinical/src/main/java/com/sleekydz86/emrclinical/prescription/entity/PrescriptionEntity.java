package com.sleekydz86.emrclinical.prescription.entity;


import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Prescription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrescriptionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", referencedColumnName = "treatment_id", nullable = false)
    @NotNull(message = "진료 정보는 필수입니다.")
    private TreatmentEntity treatmentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    @NotNull(message = "환자 정보는 필수입니다.")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_doc", referencedColumnName = "id", nullable = false)
    @NotNull(message = "처방 의사는 필수입니다.")
    private UserEntity prescriptionDoc;

    @Column(name = "prescription_date", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime prescriptionDate;

    @Column(name = "prescription_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PrescriptionStatus prescriptionStatus;

    @Column(name = "prescription_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PrescriptionType prescriptionType;

    @Column(name = "prescription_memo", columnDefinition = "TEXT")
    private String prescriptionMemo;

    @OneToMany(mappedBy = "prescriptionEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItemEntity> prescriptionItems;

    @Builder
    private PrescriptionEntity(
            Long prescriptionId,
            TreatmentEntity treatmentEntity,
            PatientEntity patientEntity,
            UserEntity prescriptionDoc,
            LocalDateTime prescriptionDate,
            PrescriptionStatus prescriptionStatus,
            PrescriptionType prescriptionType,
            String prescriptionMemo,
            List<PrescriptionItemEntity> prescriptionItems
    ) {
        validate(treatmentEntity, patientEntity, prescriptionDoc, prescriptionType);
        this.prescriptionId = prescriptionId;
        this.treatmentEntity = treatmentEntity;
        this.patientEntity = patientEntity;
        this.prescriptionDoc = prescriptionDoc;
        this.prescriptionDate = prescriptionDate != null ? prescriptionDate : LocalDateTime.now();
        this.prescriptionStatus = prescriptionStatus != null ? prescriptionStatus : PrescriptionStatus.PENDING;
        this.prescriptionType = prescriptionType;
        this.prescriptionMemo = prescriptionMemo;
        this.prescriptionItems = prescriptionItems != null ? prescriptionItems : new ArrayList<>();
    }

    private void validate(TreatmentEntity treatmentEntity, PatientEntity patientEntity,
                          UserEntity prescriptionDoc, PrescriptionType prescriptionType) {
        if (treatmentEntity == null) {
            throw new IllegalArgumentException("진료 정보는 필수입니다.");
        }
        if (patientEntity == null) {
            throw new IllegalArgumentException("환자 정보는 필수입니다.");
        }
        if (prescriptionDoc == null) {
            throw new IllegalArgumentException("처방 의사는 필수입니다.");
        }
        if (prescriptionType == null) {
            throw new IllegalArgumentException("처방 유형은 필수입니다.");
        }
    }

    public void addItem(PrescriptionItemEntity item) {
        if (item == null) {
            throw new IllegalArgumentException("처방 항목은 null일 수 없습니다.");
        }
        if (this.prescriptionStatus != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 처방만 항목을 추가할 수 있습니다.");
        }
        if (this.prescriptionItems == null) {
            this.prescriptionItems = new ArrayList<>();
        }
        item.setPrescriptionEntity(this);
        this.prescriptionItems.add(item);
    }

    public void removeItem(PrescriptionItemEntity item) {
        if (this.prescriptionStatus != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 처방만 항목을 제거할 수 있습니다.");
        }
        if (this.prescriptionItems != null) {
            this.prescriptionItems.remove(item);
            item.setPrescriptionEntity(null);
        }
    }

    public void dispense() {
        if (this.prescriptionStatus != PrescriptionStatus.PRESCRIBED) {
            throw new IllegalStateException("처방된 상태에서만 조제 완료할 수 있습니다.");
        }
        this.prescriptionStatus = PrescriptionStatus.DISPENSED;
    }

    public void cancel(String reason) {
        if (this.prescriptionStatus == PrescriptionStatus.DISPENSED) {
            throw new IllegalStateException("이미 조제된 처방은 취소할 수 없습니다.");
        }
        this.prescriptionStatus = PrescriptionStatus.CANCELLED;
        if (reason != null && !reason.isBlank()) {
            this.prescriptionMemo = reason;
        }
    }

    public void update(String memo) {
        if (this.prescriptionStatus != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 처방만 수정할 수 있습니다.");
        }
        if (memo != null && !memo.isBlank()) {
            this.prescriptionMemo = memo;
        }
    }

    public void prescribe() {
        if (this.prescriptionStatus != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 처방만 처방할 수 있습니다.");
        }
        if (this.prescriptionItems == null || this.prescriptionItems.isEmpty()) {
            throw new IllegalStateException("처방 항목이 없으면 처방할 수 없습니다.");
        }
        this.prescriptionStatus = PrescriptionStatus.PRESCRIBED;
    }

    public boolean isPending() {
        return this.prescriptionStatus == PrescriptionStatus.PENDING;
    }

    public boolean isPrescribed() {
        return this.prescriptionStatus == PrescriptionStatus.PRESCRIBED;
    }

    public boolean isDispensed() {
        return this.prescriptionStatus == PrescriptionStatus.DISPENSED;
    }

    public boolean isCancelled() {
        return this.prescriptionStatus == PrescriptionStatus.CANCELLED;
    }

    public boolean hasItems() {
        return this.prescriptionItems != null && !this.prescriptionItems.isEmpty();
    }
}

