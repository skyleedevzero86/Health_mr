package com.sleekydz86.domain.user.dto;

import com.sleekydz86.domain.user.entity.EmploymentHistoryEntity;
import com.sleekydz86.domain.user.type.AccountStatus;
import com.sleekydz86.domain.user.type.RoleType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmploymentHistoryResponse(
        Long id,
        Long userId,
        Integer tenureSequence,
        String employeeNo,
        String inttCd,
        Long departmentId,
        String departmentName,
        RoleType role,
        AccountStatus employmentStatus,
        LocalDateTime hireDate,
        LocalDateTime retireDate,
        String memo,
        LocalDateTime createdDate
) {
    public static EmploymentHistoryResponse from(EmploymentHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return EmploymentHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .tenureSequence(entity.getTenureSequence())
                .employeeNo(entity.getEmployeeNo())
                .inttCd(entity.getInttCd())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .role(entity.getRole())
                .employmentStatus(entity.getEmploymentStatus())
                .hireDate(entity.getHireDate())
                .retireDate(entity.getRetireDate())
                .memo(entity.getMemo())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
