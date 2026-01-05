package com.sleekydz86.support.attendance.strategy;

import com.sleekydz86.support.attendance.type.AttendanceType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class CheckInValidationStrategy implements AttendanceValidationStrategy {

    @Override
    public boolean supports(AttendanceType type) {
        return type == AttendanceType.CHECK_IN;
    }

    @Override
    public void validate(LocalDateTime attendanceTime, LocalDateTime endTime, String location) {
        if (attendanceTime == null) {
            throw new IllegalArgumentException("출근 시간은 필수입니다.");
        }
    }
}
