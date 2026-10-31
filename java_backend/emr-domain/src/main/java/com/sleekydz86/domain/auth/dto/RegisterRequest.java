package com.sleekydz86.domain.auth.dto;

import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.LoginId;
import com.sleekydz86.domain.common.valueobject.Password;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.Gender;
import com.sleekydz86.domain.user.type.RoleType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Getter
public class RegisterRequest {

    @NotBlank(message = "사원번호는 필수항목입니다.")
    @Size(max = 30)
    private String employeeNo;

    @NotNull(message = "부서는 필수항목입니다.")
    private Long departmentId;

    @NotBlank(message = "이름은 필수항목입니다.")
    @Size(max = 50)
    private String name;

    @NotNull(message = "성별은 필수항목입니다.")
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

    @NotBlank(message = "이메일은 필수항목입니다.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "유효한 이메일을 입력해주세요.")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "휴대폰 번호는 필수항목입니다.")
    @Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "핸드폰 번호의 양식을 확인해주세요. 예: 010-1234-5678")
    @Size(max = 20)
    private String telNum;

    @NotNull(message = "생년월일은 필수항목입니다.")
    private LocalDate birth;

    @NotNull(message = "입사일은 필수항목입니다.")
    private LocalDate hireDate;

    @NotBlank(message = "기관 코드는 필수항목입니다.")
    @Size(max = 10)
    private String inttCd;

    public UserEntity toEntity(DepartmentEntity department, PasswordEncoder passwordEncoder) {
        LoginId loginIdObj = LoginId.of(this.loginId);
        Password passwordObj = Password.fromPlainText(this.password, passwordEncoder);
        Email emailObj = this.email != null ? Email.of(this.email) : null;
        PhoneNumber telNumObj = this.telNum != null ? PhoneNumber.of(this.telNum) : null;

        return UserEntity.builder()
                .role(RoleType.WAIT)
                .employeeNo(this.employeeNo)
                .loginId(loginIdObj)
                .password(passwordObj)
                .department(department)
                .name(this.name)
                .gender(this.gender)
                .address(this.address)
                .email(emailObj)
                .telNum(telNumObj)
                .birth(this.birth != null ? this.birth.atStartOfDay() : null)
                .hireDate(this.hireDate != null ? this.hireDate.atStartOfDay() : null)
                .inttCd(this.inttCd)
                .build();
    }
}

