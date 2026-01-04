package com.sleekydz86.support.attendance.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class AttendanceTime {

    @Column(name = "attendance_time", nullable = false)
    private LocalDateTime value;

    private AttendanceTime(LocalDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("근태 시간은 필수입니다.");
        }
        this.value = value;
    }

    public static AttendanceTime of(LocalDateTime dateTime) {
        return new AttendanceTime(dateTime);
    }

    public LocalDate getDate() {
        return value.toLocalDate();
    }

    public LocalDateTime getValue() {
        return value;
    }
}

