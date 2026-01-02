package com.sleekydz86.emrclinical.checkin.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "CheckIn")
@Table(name = "check_in")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckInEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkIn_id", nullable = false)
    private Long checkInId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    @NotNull(message = "환자 정보는 필수입니다.")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity userEntity;

    @Column(name = "checkIn_date", nullable = false)
    @NotNull(message = "접수 날짜/시간은 필수입니다.")
    private LocalDateTime checkInDate;

    @Column(name = "checkIn_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private CheckInStatus checkInStatus;

    @Column(name = "checkIn_comment", length = 500)
    private String checkInComment;

    @Builder
    private CheckInEntity(
            Long checkInId,
            PatientEntity patientEntity,
            UserEntity userEntity,
            LocalDateTime checkInDate,
            CheckInStatus checkInStatus,
            String checkInComment
    ) {
        this.checkInId = checkInId;
        this.patientEntity = patientEntity;
        this.userEntity = userEntity;
        this.checkInDate = checkInDate != null ? checkInDate : LocalDateTime.now();
        this.checkInStatus = checkInStatus != null ? checkInStatus : CheckInStatus.PENDING;
        this.checkInComment = checkInComment;
    }

    public void complete() {
        this.checkInStatus = CheckInStatus.COMPLETED;
    }

    public void cancel() {
        this.checkInStatus = CheckInStatus.CANCELLED;
    }
}

