package com.sleekydz86.domain.department.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.department.type.DepartmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "department_code", nullable = false)
    @NotBlank
    @Size(max = 10)
    private String code;

    @Column(unique = true, name = "department_name", nullable = false)
    @NotBlank
    @Size(max = 100)
    private String name;

    @Column(unique = true, name = "department_eng_name")
    @Size(max = 100)
    private String engName;

    @Column(name = "department_type")
    @Enumerated(EnumType.STRING)
    @NotNull
    private DepartmentType type;
}