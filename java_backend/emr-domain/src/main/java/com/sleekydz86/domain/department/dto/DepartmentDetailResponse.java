package com.sleekydz86.domain.department.dto;

import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.type.DepartmentType;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record DepartmentDetailResponse(
        Long id,
        String code,
        String name,
        String engName,
        DepartmentType type,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
    public static DepartmentDetailResponse from(DepartmentEntity entity) {
        return DepartmentDetailResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .engName(entity.getEngName())
                .type(entity.getType())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}