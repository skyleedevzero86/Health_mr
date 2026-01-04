package com.sleekydz86.support.attendance.strategy;

import com.sleekydz86.support.attendance.type.AttendanceType;
import java.time.LocalDateTime;

public interface AttendanceValidationStrategy {
    boolean supports(AttendanceType type);
    void validate(LocalDateTime attendanceTime, LocalDateTime endTime, String location);
}

