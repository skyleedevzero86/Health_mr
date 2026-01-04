package com.sleekydz86.support.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.attendance.entity.AttendanceEntity;
import com.sleekydz86.support.attendance.type.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long attendanceId;
    private Long userId;
    private String userName;
    private AttendanceType attendanceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime attendanceTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    private String location;
    private String remarks;

    public static AttendanceResponse from(AttendanceEntity entity) {
        return new AttendanceResponse(
                entity.getAttendanceId(),
                entity.getUserEntity().getId(),
                entity.getUserEntity().getName(),
                entity.getAttendanceType(),
                entity.getAttendanceTime().getValue(),
                entity.getEndTime(),
                entity.getLocation(),
                entity.getRemarks()
        );
    }
}
