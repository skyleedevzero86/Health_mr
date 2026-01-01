package com.sleekydz86.domain.user.dto;

import com.sleekydz86.domain.user.entity.UserEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WaitUserResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String telNum,
        LocalDateTime registerDate,
        Long departmentId,
        String departmentName
) {
    public static WaitUserResponse from(UserEntity entity) {
        return WaitUserResponse.builder()
                .id(entity.getId())
                .loginId(entity.getLoginIdValue())
                .name(entity.getName())
                .email(entity.getEmailValue())
                .telNum(entity.getTelNumValue())
                .registerDate(entity.getCreatedDate())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .build();
    }
}

