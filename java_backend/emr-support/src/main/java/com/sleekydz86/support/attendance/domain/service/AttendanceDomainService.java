package com.sleekydz86.support.attendance.domain.service;

import com.sleekydz86.support.attendance.entity.AttendanceEntity;
import com.sleekydz86.support.attendance.type.AttendanceType;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AttendanceDomainService {

    public void validateAttendanceTime(AttendanceType type, LocalDateTime attendanceTime, LocalDateTime endTime) {
        if (attendanceTime == null) {
            throw new IllegalArgumentException("근태 시간은 필수입니다.");
        }

        if (endTime != null && endTime.isBefore(attendanceTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }

        if (type == AttendanceType.CHECK_OUT && endTime == null) {
            throw new IllegalArgumentException("퇴근 시 종료 시간은 필수입니다.");
        }
    }

    public boolean canRegisterAttendance(AttendanceEntity lastAttendance, AttendanceType newType) {
        if (lastAttendance == null) {
            return true;
        }

        AttendanceType lastType = lastAttendance.getAttendanceType();

        if (newType == AttendanceType.CHECK_IN) {
            return lastType == AttendanceType.CHECK_OUT || lastType == null;
        }

        if (newType == AttendanceType.CHECK_OUT) {
            return lastType == AttendanceType.CHECK_IN;
        }

        return true;
    }
}

