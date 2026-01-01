package com.sleekydz86.domain.department.dto;

import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.type.DepartmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DepartmentCreateRequest {
    @NotBlank(message = "부서 코드는 필수항목입니다.")
    @Size(max = 10)
    private String code;

    @NotBlank(message = "부서명은 필수항목입니다.")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String engName;

    @NotNull(message = "부서 타입은 필수항목입니다.")
    private DepartmentType type;

    public DepartmentEntity toEntity() {
        return DepartmentEntity.builder()
                .code(this.code)
                .name(this.name)
                .engName(this.engName)
                .type(this.type)
                .build();
    }
}

