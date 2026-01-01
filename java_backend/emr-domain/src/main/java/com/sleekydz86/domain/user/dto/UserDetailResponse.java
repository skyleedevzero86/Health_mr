package com.sleekydz86.domain.user.dto;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.Gender;
import com.sleekydz86.domain.user.type.RoleType;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record UserDetailResponse(
        Long id,
        RoleType role,
        String loginId,
        String name,
        Gender gender,
        String address,
        String email,
        String telNum,
        LocalDateTime birth,
        LocalDateTime hireDate,
        LocalDateTime registerDate,
        Long departmentId,
        String departmentName,
        String inttCd
) {
    public static UserDetailResponse from(UserEntity entity) {
        return UserDetailResponse.builder()
                .id(entity.getId())
                .role(entity.getRole())
                .loginId(entity.getLoginIdValue())
                .name(entity.getName())
                .gender(entity.getGender())
                .address(entity.getAddress())
                .email(entity.getEmailValue())
                .telNum(entity.getTelNumValue())
                .birth(entity.getBirth())
                .hireDate(entity.getHireDate())
                .registerDate(entity.getCreatedDate())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .inttCd(entity.getInttCd())
                .build();
    }
}

