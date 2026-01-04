package com.sleekydz86.support.attendance.dto;

import com.sleekydz86.support.attendance.type.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatisticsResponse {

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    private String userName;

    private Long totalAttendances;
    private Long totalCheckIns;
    private Long totalCheckOuts;
    private Long totalWorkingMinutes;

    private Map<AttendanceType, Long> attendanceTypeStatistics;

    private LocalTime averageCheckInTime;
    private LocalTime averageCheckOutTime;
    private Long averageWorkingMinutes;

    private Long totalLeaves;
    private Long approvedLeaves;
    private Long pendingLeaves;
    private Long rejectedLeaves;
}

