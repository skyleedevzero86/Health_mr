package com.sleekydz86.support.attendance.strategy;

import com.sleekydz86.support.attendance.type.AttendanceType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class CheckOutValidationStrategy implements AttendanceValidationStrategy {

    @Override
    public boolean supports(AttendanceType type) {
        return type == AttendanceType.CHECK_OUT;
    }

    @Override
    public void validate(LocalDateTime attendanceTime, LocalDateTime endTime, String location) {
        if (attendanceTime == null) {
            throw new IllegalArgumentException("퇴근 시간은 필수입니다.");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("퇴근 시 종료 시간은 필수입니다.");
        }
        if (endTime.isBefore(attendanceTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
    }
}

