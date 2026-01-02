package com.sleekydz86.emrclinical.reservation.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "Reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    @NotNull(message = "환자 정보는 필수입니다.")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity userEntity;

    @Column(name = "reservation_datetime", nullable = false)
    @NotNull(message = "예약 날짜/시간은 필수입니다.")
    private LocalDateTime reservationDate;

    @Column(name = "reservation_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ReservationStatus reservationStatus;

    @Deprecated
    @Column(name = "reservation_YN", length = 1)
    private String reservationYn;

    @Column(name = "reservation_change_datetime")
    private LocalDateTime reservationChangeDate;

    @Column(name = "reservation_change_cause", length = 500)
    private String reservationChangeCause;

    @Builder
    private ReservationEntity(
            Long reservationId,
            PatientEntity patientEntity,
            UserEntity userEntity,
            LocalDateTime reservationDate,
            ReservationStatus reservationStatus,
            String reservationYn,
            LocalDateTime reservationChangeDate,
            String reservationChangeCause
    ) {
        validate(patientEntity, reservationDate);
        this.reservationId = reservationId;
        this.patientEntity = patientEntity;
        this.userEntity = userEntity;
        this.reservationDate = reservationDate;
        this.reservationStatus = reservationStatus != null ? reservationStatus : ReservationStatus.PENDING;
        this.reservationYn = reservationYn;
        this.reservationChangeDate = reservationChangeDate;
        this.reservationChangeCause = reservationChangeCause;
    }

    private void validate(PatientEntity patientEntity, LocalDateTime reservationDate) {
        if (patientEntity == null) {
            throw new IllegalArgumentException("환자 정보는 필수입니다.");
        }
        if (reservationDate == null) {
            throw new IllegalArgumentException("예약 날짜/시간은 필수입니다.");
        }
        if (reservationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거 날짜는 예약할 수 없습니다.");
        }
    }

    public void confirm() {
        if (this.reservationStatus != ReservationStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 예약만 확인할 수 있습니다.");
        }
        this.reservationStatus = ReservationStatus.CONFIRMED;
        this.reservationYn = "Y";
    }

    public void cancel(String reason) {
        if (this.reservationStatus == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 예약은 취소할 수 없습니다.");
        }
        this.reservationStatus = ReservationStatus.CANCELLED;
        this.reservationYn = "N";
        this.reservationChangeCause = reason;
        this.reservationChangeDate = LocalDateTime.now();
    }

    public void complete() {
        if (this.reservationStatus != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("확인된 예약만 완료할 수 있습니다.");
        }
        this.reservationStatus = ReservationStatus.COMPLETED;
    }


    public void update(LocalDateTime newDate, String reason) {
        if (this.reservationStatus == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("완료된 예약은 변경할 수 없습니다.");
        }
        if (this.reservationStatus == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약은 변경할 수 없습니다.");
        }
        if (newDate != null) {
            if (newDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("과거 날짜로는 변경할 수 없습니다.");
            }
            this.reservationDate = newDate;
        }
        if (reason != null && !reason.isBlank()) {
            this.reservationChangeCause = reason;
        }
        this.reservationChangeDate = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.reservationStatus == ReservationStatus.PENDING;
    }

    public boolean isConfirmed() {
        return this.reservationStatus == ReservationStatus.CONFIRMED;
    }

    public boolean isCompleted() {
        return this.reservationStatus == ReservationStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.reservationStatus == ReservationStatus.CANCELLED;
    }

    public boolean isFuture() {
        return this.reservationDate.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return this.reservationDate.isBefore(LocalDateTime.now());
    }
}

