package com.sleekydz86.support.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.attendance.entity.LeaveEntity;
import com.sleekydz86.support.attendance.type.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private Long leaveId;
    private Long userId;
    private String userName;
    private LeaveType leaveType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String reason;
    private String status;

    public static LeaveResponse from(LeaveEntity entity) {
        return new LeaveResponse(
                entity.getLeaveId(),
                entity.getUserEntity().getId(),
                entity.getUserEntity().getName(),
                entity.getLeaveType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getReason(),
                entity.getStatus()
        );
    }
}

