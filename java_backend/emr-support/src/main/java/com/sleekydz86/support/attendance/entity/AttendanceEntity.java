package com.sleekydz86.support.attendance.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.support.attendance.valueobject.AttendanceTime;
import com.sleekydz86.support.attendance.type.AttendanceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "Attendance")
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type", nullable = false, length = 20)
    private AttendanceType attendanceType;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "attendance_time", nullable = false))
    private AttendanceTime attendanceTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Builder
    private AttendanceEntity(
            Long attendanceId,
            UserEntity userEntity,
            AttendanceType attendanceType,
            AttendanceTime attendanceTime,
            LocalDateTime endTime,
            String location,
            String remarks) {
        this.attendanceId = attendanceId;
        this.userEntity = userEntity;
        this.attendanceType = attendanceType;
        this.attendanceTime = attendanceTime;
        this.endTime = endTime;
        this.location = location;
        this.remarks = remarks;
    }

    public void updateEndTime(LocalDateTime endTime) {
        if (endTime != null && this.attendanceTime.getValue().isAfter(endTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
        this.endTime = endTime;
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updateRemarks(String remarks) {
        this.remarks = remarks;
    }

}
