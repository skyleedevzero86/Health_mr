package com.sleekydz86.support.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.attendance.type.AttendanceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRegisterRequest {

    @NotNull(message = "근태 타입은 필수 값입니다.")
    private AttendanceType attendanceType;

    @NotNull(message = "근태 시간은 필수 값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime attendanceTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    private String location;

    private String remarks;
}
