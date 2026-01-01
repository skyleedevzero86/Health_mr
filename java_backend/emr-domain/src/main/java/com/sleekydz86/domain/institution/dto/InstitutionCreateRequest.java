package com.sleekydz86.domain.institution.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class InstitutionCreateRequest {

    @NotBlank(message = "기관 코드는 필수항목입니다.")
    @Size(max = 10, message = "기관 코드는 10자 이하여야 합니다.")
    private String institutionCode;

    @NotBlank(message = "기관명은 필수항목입니다.")
    @Size(max = 100, message = "기관명은 100자 이하여야 합니다.")
    private String institutionName;

    @Size(max = 100, message = "기관 영문명은 100자 이하여야 합니다.")
    private String institutionEngName;

    @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
    private String institutionAddress;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String institutionTel;

    @Email(message = "유효한 이메일을 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String institutionEmail;

    @Size(max = 50, message = "기관장명은 50자 이하여야 합니다.")
    private String directorName;
}

