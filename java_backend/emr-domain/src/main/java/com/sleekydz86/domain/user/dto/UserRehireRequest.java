package com.sleekydz86.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserRehireRequest {
    @NotBlank(message = "새 사원번호는 필수항목입니다.")
    @Size(max = 30)
    private String employeeNo;

    @NotBlank(message = "기관 코드는 필수항목입니다.")
    @Size(max = 10)
    private String inttCd;

    @NotNull(message = "부서는 필수항목입니다.")
    private Long departmentId;

    @NotNull(message = "재입사일은 필수항목입니다.")
    private LocalDate hireDate;
}
