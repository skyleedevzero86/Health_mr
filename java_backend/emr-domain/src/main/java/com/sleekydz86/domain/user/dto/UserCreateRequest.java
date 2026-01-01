package com.sleekydz86.domain.user.dto;

import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.LoginId;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.Gender;
import com.sleekydz86.domain.user.type.RoleType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UserCreateRequest {

    @NotNull(message = "부서는 필수항목입니다.")
    private Long departmentId;

    @NotBlank(message = "이름은 필수항목입니다.")
    @Size(max = 50)
    private String name;

    private Gender gender;

    @Size(max = 50, min = 3)
    @NotBlank(message = "아이디는 필수항목입니다.")
    private String loginId;

    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "비밀번호는 영문 대소문자, 숫자 또는 특수문자 중 2가지 이상 조합, 8자 이상 16자 이하로 설정해야 합니다.")
    @NotBlank(message = "비밀번호는 필수항목입니다.")
    private String password;

    @Size(max = 200)
    private String address;

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "유효한 이메일을 입력해주세요.")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "핸드폰 번호의 양식을 확인해주세요. 예: 010-1234-5678")
    @Size(max = 20)
    private String telNum;

    private LocalDate birth;

    private LocalDate hireDate;

    @Size(max = 10)
    private String inttCd;

    @NotNull(message = "역할은 필수항목입니다.")
    private RoleType role;

    public UserEntity.UserEntityBuilder toEntityBuilder(DepartmentEntity department) {
        LoginId loginIdObj = LoginId.of(this.loginId);
        Email emailObj = this.email != null ? Email.of(this.email) : null;
        PhoneNumber telNumObj = this.telNum != null ? PhoneNumber.of(this.telNum) : null;

        return UserEntity.builder()
                .role(this.role != null ? this.role : RoleType.WAIT)
                .loginId(loginIdObj)
                .department(department)
                .name(this.name)
                .gender(this.gender)
                .address(this.address)
                .email(emailObj)
                .telNum(telNumObj)
                .birth(this.birth != null ? this.birth.atStartOfDay() : null)
                .hireDate(this.hireDate != null ? this.hireDate.atStartOfDay() : null)
                .inttCd(this.inttCd);
    }
}

