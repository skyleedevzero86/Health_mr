package com.sleekydz86.emrclinical.checkin.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "CheckIn")
@Table(
        name = "check_in",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_checkin_patient_day_dept",
                columnNames = {"patient_no", "checkin_day", "department_id", "dup_slot"}
        )
)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private DepartmentEntity departmentEntity;

    @Column(name = "checkIn_date", nullable = false)
    @NotNull(message = "접수 날짜/시간은 필수입니다.")
    private LocalDateTime checkInDate;

    @Column(name = "checkin_day")
    private LocalDate checkinDay;

    @Column(name = "dup_slot")
    private Integer dupSlot;

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
            DepartmentEntity departmentEntity,
            LocalDateTime checkInDate,
            CheckInStatus checkInStatus,
            String checkInComment
    ) {
        this.checkInId = checkInId;
        this.patientEntity = patientEntity;
        this.userEntity = userEntity;
        this.departmentEntity = departmentEntity;
        this.checkInDate = checkInDate != null ? checkInDate : LocalDateTime.now();
        this.checkInStatus = checkInStatus != null ? checkInStatus : CheckInStatus.PENDING;
        this.checkInComment = checkInComment;
        refreshDuplicateSlot();
    }

    public void complete() {
        this.checkInStatus = CheckInStatus.COMPLETED;
        refreshDuplicateSlot();
    }

    public void cancel() {
        this.checkInStatus = CheckInStatus.CANCELLED;
        refreshDuplicateSlot();
    }

    @PrePersist
    @PreUpdate
    void refreshDuplicateSlot() {
        if (this.checkInDate != null) {
            this.checkinDay = this.checkInDate.toLocalDate();
        }
        this.dupSlot = this.checkInStatus == CheckInStatus.CANCELLED ? null : 1;
    }
}
