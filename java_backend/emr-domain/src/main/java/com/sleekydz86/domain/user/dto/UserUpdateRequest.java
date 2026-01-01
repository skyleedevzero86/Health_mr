package com.sleekydz86.domain.user.dto;

import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.user.type.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UserUpdateRequest {

    @Size(max = 50)
    private String name;

    private Gender gender;

    @Size(max = 200)
    private String address;

    @Email(message = "유효한 이메일을 입력해주세요.")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "핸드폰 번호의 양식을 확인해주세요.")
    @Size(max = 20)
    private String telNum;

    private LocalDate birth;

    private LocalDate hireDate;

    private Long departmentId;

    public Email getEmailValueObject() {
        return email != null ? Email.of(email) : null;
    }

    public PhoneNumber getTelNumValueObject() {
        return telNum != null ? PhoneNumber.of(telNum) : null;
    }
}

