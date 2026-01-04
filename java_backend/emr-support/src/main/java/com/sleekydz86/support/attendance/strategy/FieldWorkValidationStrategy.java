package com.sleekydz86.support.attendance.strategy;

import com.sleekydz86.support.attendance.type.AttendanceType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class FieldWorkValidationStrategy implements AttendanceValidationStrategy {

    @Override
    public boolean supports(AttendanceType type) {
        return type == AttendanceType.FIELD_WORK;
    }

    @Override
    public void validate(LocalDateTime attendanceTime, LocalDateTime endTime, String location) {
        if (attendanceTime == null) {
            throw new IllegalArgumentException("외근 시작 시간은 필수입니다.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("외근 시 위치 정보는 필수입니다.");
        }
    }
}

