package com.sleekydz86.domain.department.dto;

import com.sleekydz86.domain.department.type.DepartmentType;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DepartmentUpdateRequest {
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String engName;

    private DepartmentType type;
}
