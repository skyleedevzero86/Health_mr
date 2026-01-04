package com.sleekydz86.support.doctortreatment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "DoctorTreatment")
@Table(name = "doctor_treatment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DoctorTreatmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctorTreatment_id", nullable = false, unique = true)
    private Long doctorTreatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "doctorTreatment_starttime", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime doctorTreatmentStart;

    @Column(name = "doctorTreatment_endtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime doctorTreatmentEnd;

    @Builder
    private DoctorTreatmentEntity(
            Long doctorTreatmentId,
            PatientEntity patientEntity,
            UserEntity userEntity,
            LocalDateTime doctorTreatmentStart,
            LocalDateTime doctorTreatmentEnd) {
        validate(patientEntity, userEntity, doctorTreatmentStart, doctorTreatmentEnd);
        this.doctorTreatmentId = doctorTreatmentId;
        this.patientEntity = patientEntity;
        this.userEntity = userEntity;
        this.doctorTreatmentStart = doctorTreatmentStart;
        this.doctorTreatmentEnd = doctorTreatmentEnd;
    }

    private void validate(PatientEntity patientEntity, UserEntity userEntity,
                          LocalDateTime startTime, LocalDateTime endTime) {
        if (patientEntity == null) {
            throw new IllegalArgumentException("환자 정보는 필수입니다.");
        }
        if (userEntity == null) {
            throw new IllegalArgumentException("의사 정보는 필수입니다.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("진료 시작 시간은 필수입니다.");
        }
        if (endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("진료 종료 시간은 시작 시간보다 이후여야 합니다.");
        }
    }

    public void startTreatment(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("진료 시작 시간은 필수입니다.");
        }
        this.doctorTreatmentStart = startTime;
        this.doctorTreatmentEnd = null;
    }

    public void endTreatment(LocalDateTime endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("진료 종료 시간은 필수입니다.");
        }
        if (this.doctorTreatmentStart == null) {
            throw new IllegalStateException("진료가 시작되지 않았습니다.");
        }
        if (endTime.isBefore(this.doctorTreatmentStart)) {
            throw new IllegalArgumentException("진료 종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        this.doctorTreatmentEnd = endTime;
    }

    public void updateTreatmentTime(LocalDateTime startTime, LocalDateTime endTime) {
        validate(this.patientEntity, this.userEntity, startTime, endTime);
        this.doctorTreatmentStart = startTime;
        this.doctorTreatmentEnd = endTime;
    }

    public Duration calculateTreatmentDuration() {
        if (this.doctorTreatmentStart == null) {
            return Duration.ZERO;
        }
        if (this.doctorTreatmentEnd == null) {
            return Duration.between(this.doctorTreatmentStart, LocalDateTime.now());
        }
        return Duration.between(this.doctorTreatmentStart, this.doctorTreatmentEnd);
    }

    public long getTreatmentDurationInMinutes() {
        return calculateTreatmentDuration().toMinutes();
    }

    public boolean isInProgress() {
        return this.doctorTreatmentStart != null && this.doctorTreatmentEnd == null;
    }

    public boolean isCompleted() {
        return this.doctorTreatmentStart != null && this.doctorTreatmentEnd != null;
    }
}
