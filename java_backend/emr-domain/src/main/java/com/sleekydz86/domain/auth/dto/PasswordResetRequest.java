package com.sleekydz86.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetRequest {
    @NotBlank(message = "이메일은 필수항목입니다.")
    @Email(message = "유효한 이메일을 입력해주세요.")
    private String email;
}
