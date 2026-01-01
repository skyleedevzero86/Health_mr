package com.sleekydz86.domain.patient.dto;

import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PatientUpdateRequest {
    @Size(max = 50)
    private String patientName;

    @Size(max = 200)
    private String patientAddress;

    @Email(message = "유효한 이메일을 입력해주세요.")
    @Size(max = 100)
    private String patientEmail;

    @Size(max = 20)
    private String patientTel;

    @Pattern(regexp = "^[YN]$", message = "Y 또는 N만 입력 가능합니다.")
    private String patientHypassYN;

    @Size(max = 50)
    private String guardian;

    public Email getEmailValueObject() {
        return patientEmail != null ? Email.of(patientEmail) : null;
    }

    public PhoneNumber getTelValueObject() {
        return patientTel != null ? PhoneNumber.of(patientTel) : null;
    }
}

