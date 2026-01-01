package com.sleekydz86.domain.patient.dto;

import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.common.valueobject.ResidentRegistrationNumber;
import com.sleekydz86.domain.patient.entity.PatientEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class PatientRegisterRequest {

    @NotBlank(message = "환자 이름은 필수항목입니다.")
    @Size(max = 50)
    private String patientName;

    @NotBlank(message = "주민등록번호는 필수항목입니다.")
    @Pattern(regexp = "^\\d{6}-\\d{7}$", message = "주민등록번호 형식이 올바르지 않습니다.")
    private String patientRrn;

    @NotBlank(message = "성별은 필수항목입니다.")
    @Size(max = 10)
    private String patientGender;

    private LocalDate patientBirth;

    @Size(max = 200)
    private String patientAddress;

    @NotBlank(message = "이메일은 필수항목입니다.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "유효한 이메일을 입력해주세요.")
    @Size(max = 100)
    private String patientEmail;

    @NotBlank(message = "전화번호는 필수항목입니다.")
    @Size(max = 20)
    private String patientTel;

    @NotBlank(message = "외국인 여부는 필수항목입니다.")
    @Pattern(regexp = "^[YN]$", message = "Y 또는 N만 입력 가능합니다.")
    private String patientForeign;

    @Size(max = 50)
    private String patientPassport;

    @Pattern(regexp = "^[YN]$", message = "Y 또는 N만 입력 가능합니다.")
    private String patientHypassYN;

    @Size(max = 50)
    private String guardian;

    public PatientEntity toEntity(PatientNumber patientNo) {
        return PatientEntity.builder()
                .patientNo(patientNo)
                .patientName(this.patientName)
                .patientRrn(ResidentRegistrationNumber.of(this.patientRrn))
                .patientGender(this.patientGender)
                .patientBirth(this.patientBirth)
                .patientAddress(this.patientAddress)
                .patientEmail(Email.of(this.patientEmail))
                .patientTel(PhoneNumber.of(this.patientTel))
                .isForeign("Y".equals(this.patientForeign))
                .patientPassport(this.patientPassport)
                .hasHypass("Y".equals(this.patientHypassYN))
                .guardian(this.guardian)
                .build();
    }
}

