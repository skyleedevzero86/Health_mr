package com.sleekydz86.support.recess.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "DoctorRecessEntity")
@Table(name = "recess")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "recess_start", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recessStart;

    @Column(name = "recess_end", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recessEnd;

    @Column(name = "recess_reason", length = 255)
    private String recessReason;

    @Builder
    private RecessEntity(
            Long id,
            UserEntity userEntity,
            LocalDateTime recessStart,
            LocalDateTime recessEnd,
            String recessReason
    ) {
        validate(userEntity, recessStart, recessEnd);
        this.id = id;
        this.userEntity = userEntity;
        this.recessStart = adjustToHalfHour(recessStart);
        this.recessEnd = adjustToHalfHour(recessEnd);
        this.recessReason = recessReason;
    }

    private void validate(UserEntity userEntity, LocalDateTime startTime, LocalDateTime endTime) {
        if (userEntity == null) {
            throw new IllegalArgumentException("사용자 정보는 필수입니다.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("휴진 시작 시간은 필수입니다.");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("휴진 종료 시간은 필수입니다.");
        }
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("휴진 종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        validateTimeSlot(startTime, endTime);
    }


    private LocalDateTime adjustToHalfHour(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        int adjustedMinute = (minute / 30) * 30;
        return dateTime.withMinute(adjustedMinute).withSecond(0).withNano(0);
    }

    private void validateTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes();

        if (minutes < 30) {
            throw new IllegalArgumentException("휴진 시간은 최소 30분 이상이어야 합니다.");
        }

        if (minutes % 30 != 0) {
            throw new IllegalArgumentException("휴진 시간은 30분 단위여야 합니다.");
        }
    }

    public void updateRecessTime(LocalDateTime startTime, LocalDateTime endTime) {
        validate(this.userEntity, startTime, endTime);
        this.recessStart = adjustToHalfHour(startTime);
        this.recessEnd = adjustToHalfHour(endTime);
    }

    public void updateReason(String reason) {
        this.recessReason = reason;
    }

    public Duration calculateRecessDuration() {
        return Duration.between(this.recessStart, this.recessEnd);
    }

    public long getRecessDurationInMinutes() {
        return calculateRecessDuration().toMinutes();
    }

    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(this.recessStart) && !now.isAfter(this.recessEnd);
    }

    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(this.recessStart) || LocalDateTime.now().isEqual(this.recessStart);
    }

    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(this.recessEnd);
    }

    public boolean isFuture() {
        return LocalDateTime.now().isBefore(this.recessStart);
    }

    public boolean contains(LocalDateTime dateTime) {
        return !dateTime.isBefore(this.recessStart) && !dateTime.isAfter(this.recessEnd);
    }
}