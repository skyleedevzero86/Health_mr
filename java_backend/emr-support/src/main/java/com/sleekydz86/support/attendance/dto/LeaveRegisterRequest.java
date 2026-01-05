package com.sleekydz86.support.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.attendance.type.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRegisterRequest {

    @NotNull(message = "휴가 타입은 필수 값입니다.")
    private LeaveType leaveType;

    @NotNull(message = "시작 날짜는 필수 값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수 값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String reason;
}

