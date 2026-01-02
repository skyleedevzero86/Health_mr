package com.sleekydz86.emrclinical.treatment.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "Treatments")
@Table(name = "treatments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id", nullable = false)
    private Long treatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkIn_id", referencedColumnName = "checkIn_id")
    private CheckInEntity checkInEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_doc", referencedColumnName = "id", nullable = false)
    private UserEntity treatmentDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private DepartmentEntity departmentEntity;

    @Column(name = "treatment_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TreatmentType treatmentType;

    @Column(name = "treatment_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TreatmentStatus treatmentStatus;

    @Column(name = "treatment_date", nullable = false)
    private LocalDateTime treatmentDate;

    @Column(name = "treatment_start_time")
    private LocalDateTime treatmentStartTime;

    @Column(name = "treatment_end_time")
    private LocalDateTime treatmentEndTime;

    @Column(name = "treatment_comment", columnDefinition = "TEXT")
    private String treatmentComment;

    @Column(name = "treatment_dept", length = 100)
    private String treatmentDept;

    @Builder
    private TreatmentEntity(
            Long treatmentId,
            CheckInEntity checkInEntity,
            PatientEntity patientEntity,
            UserEntity treatmentDoc,
            DepartmentEntity departmentEntity,
            TreatmentType treatmentType,
            TreatmentStatus treatmentStatus,
            LocalDateTime treatmentDate,
            LocalDateTime treatmentStartTime,
            LocalDateTime treatmentEndTime,
            String treatmentComment,
            String treatmentDept
    ) {
        this.treatmentId = treatmentId;
        this.checkInEntity = checkInEntity;
        this.patientEntity = patientEntity;
        this.treatmentDoc = treatmentDoc;
        this.departmentEntity = departmentEntity;
        this.treatmentType = treatmentType;
        this.treatmentStatus = treatmentStatus != null ? treatmentStatus : TreatmentStatus.PENDING;
        this.treatmentDate = treatmentDate != null ? treatmentDate : LocalDateTime.now();
        this.treatmentStartTime = treatmentStartTime;
        this.treatmentEndTime = treatmentEndTime;
        this.treatmentComment = treatmentComment;
        this.treatmentDept = treatmentDept;
    }

    public void update(String comment, DepartmentEntity department, UserEntity doctor) {
        if (comment != null && !comment.isBlank()) {
            this.treatmentComment = comment;
        }
        if (department != null) {
            this.departmentEntity = department;
            this.treatmentDept = department.getName();
        }
        if (doctor != null) {
            this.treatmentDoc = doctor;
        }
    }

    public void start() {
        if (this.treatmentStatus == TreatmentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 진료는 시작할 수 없습니다.");
        }
        if (this.treatmentStatus == TreatmentStatus.CANCELLED) {
            throw new IllegalStateException("취소된 진료는 시작할 수 없습니다.");
        }
        this.treatmentStatus = TreatmentStatus.IN_PROGRESS;
        this.treatmentStartTime = LocalDateTime.now();
    }

    public void complete(String comment) {
        if (this.treatmentStatus == TreatmentStatus.CANCELLED) {
            throw new IllegalStateException("취소된 진료는 완료할 수 없습니다.");
        }
        this.treatmentStatus = TreatmentStatus.COMPLETED;
        this.treatmentEndTime = LocalDateTime.now();
        if (comment != null && !comment.isBlank()) {
            this.treatmentComment = comment;
        }
    }

    public void cancel(String reason) {
        if (this.treatmentStatus == TreatmentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 진료는 취소할 수 없습니다.");
        }
        this.treatmentStatus = TreatmentStatus.CANCELLED;
        if (reason != null && !reason.isBlank()) {
            this.treatmentComment = reason;
        }
    }
}
