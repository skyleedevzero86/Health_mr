package com.sleekydz86.support.attendance.strategy;

import com.sleekydz86.support.attendance.type.AttendanceType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DefaultValidationStrategy implements AttendanceValidationStrategy {

    @Override
    public boolean supports(AttendanceType type) {
        return type == AttendanceType.EDUCATION ||
               type == AttendanceType.AWAY ||
               type == AttendanceType.MEAL;
    }

    @Override
    public void validate(LocalDateTime attendanceTime, LocalDateTime endTime, String location) {
        if (attendanceTime == null) {
            throw new IllegalArgumentException("근태 시간은 필수입니다.");
        }
    }
}

