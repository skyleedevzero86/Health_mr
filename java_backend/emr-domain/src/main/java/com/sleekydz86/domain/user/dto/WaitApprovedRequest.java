package com.sleekydz86.domain.user.dto;

import com.sleekydz86.domain.user.type.RoleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import java.util.List;

@Getter
public class WaitApprovedRequest {
    @NotNull(message = "사용자 ID는 필수항목입니다.")
    private Long userId;

    @NotNull(message = "역할은 필수항목입니다.")
    private RoleType role;

    @Size(max = 3, message = "병원은 최대 3개까지 지정할 수 있습니다.")
    private List<String> institutionCodes;

    private String primaryInstitutionCode;
}

