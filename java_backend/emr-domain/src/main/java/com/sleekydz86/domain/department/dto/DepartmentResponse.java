package com.sleekydz86.domain.department.dto;

import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.type.DepartmentType;
import lombok.Builder;

@Builder
public record DepartmentResponse(
        Long id,
        String code,
        String name,
        String engName,
        DepartmentType type
) {
    public static DepartmentResponse from(DepartmentEntity entity) {
        return DepartmentResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .engName(entity.getEngName())
                .type(entity.getType())
                .build();
    }
}
