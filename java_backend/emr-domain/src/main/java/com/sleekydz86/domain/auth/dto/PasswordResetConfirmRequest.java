package com.sleekydz86.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordResetConfirmRequest {
    @NotBlank(message = "이메일은 필수항목입니다.")
    private String email;

    @NotBlank(message = "인증 코드는 필수항목입니다.")
    private String code;

    @NotBlank(message = "비밀번호는 필수항목입니다.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하이어야 합니다.")
    private String newPassword;
}
