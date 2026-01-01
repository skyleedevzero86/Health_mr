package com.sleekydz86.domain.patient.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.common.valueobject.ResidentRegistrationNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity(name = "Patient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatientEntity extends BaseEntity {

    @Id
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "patient_no", nullable = false, unique = true, length = 8))
    private PatientNumber patientNo;

    @Column(name = "patient_name", nullable = false)
    @NotBlank
    @Size(max = 50)
    private String patientName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "patient_rrn", nullable = false, unique = true, length = 14))
    private ResidentRegistrationNumber patientRrn;

    @Column(name = "patient_gender", nullable = false)
    @NotBlank
    @Size(max = 10)
    private String patientGender;

    @Column(name = "patient_birth")
    private LocalDate patientBirth;

    @Column(name = "patient_address")
    @Size(max = 200)
    private String patientAddress;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "patient_email", nullable = false, unique = true, length = 100))
    private Email patientEmail;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "patient_tel", nullable = false, unique = true, length = 20))
    private PhoneNumber patientTel;

    @Column(name = "patient_foreign", nullable = false, length = 1)
    private Boolean isForeign;

    @Column(name = "patient_passport", unique = true, length = 50)
    @Size(max = 50)
    private String patientPassport;

    @Column(name = "patient_hypass_YN", length = 1)
    private Boolean hasHypass;

    @Column(name = "patient_last_visit")
    private LocalDate patientLastVisit;

    @Size(max = 50)
    private String guardian;

    @Builder
    private PatientEntity(
            PatientNumber patientNo,
            String patientName,
            ResidentRegistrationNumber patientRrn,
            String patientGender,
            LocalDate patientBirth,
            String patientAddress,
            Email patientEmail,
            PhoneNumber patientTel,
            Boolean isForeign,
            String patientPassport,
            Boolean hasHypass,
            LocalDate patientLastVisit,
            String guardian
    ) {
        this.patientNo = patientNo;
        this.patientName = patientName;
        this.patientRrn = patientRrn;
        this.patientGender = patientGender;
        this.patientBirth = patientBirth;
        this.patientAddress = patientAddress;
        this.patientEmail = patientEmail;
        this.patientTel = patientTel;
        this.isForeign = isForeign != null ? isForeign : false;
        this.patientPassport = patientPassport;
        this.hasHypass = hasHypass;
        this.patientLastVisit = patientLastVisit;
        this.guardian = guardian;
    }

    public void updateLastVisit() {
        this.patientLastVisit = LocalDate.now();
    }

    public void updateInfo(String name, String address, Email email, PhoneNumber tel) {
        if (name != null && !name.isBlank()) {
            this.patientName = name;
        }
        if (address != null) {
            this.patientAddress = address;
        }
        if (email != null) {
            this.patientEmail = email;
        }
        if (tel != null) {
            this.patientTel = tel;
        }
    }

    public void changeEmail(Email newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        this.patientEmail = newEmail;
    }

    public void changePhoneNumber(PhoneNumber newPhoneNumber) {
        if (newPhoneNumber == null) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        this.patientTel = newPhoneNumber;
    }

    public void setForeign(boolean isForeign) {
        this.isForeign = isForeign;
        if (!isForeign) {
            this.patientPassport = null;
        }
    }

    public void setHypass(boolean hasHypass) {
        this.hasHypass = hasHypass;
    }

    public void setGuardian(String guardian) {
        this.guardian = guardian;
    }

    public boolean isForeign() {
        return Boolean.TRUE.equals(isForeign);
    }

    public boolean hasHypass() {
        return Boolean.TRUE.equals(hasHypass);
    }

    public Long getPatientNoValue() {
        return patientNo != null ? patientNo.getValue() : null;
    }

    public String getPatientRrnValue() {
        return patientRrn != null ? patientRrn.getValue() : null;
    }

    public String getPatientEmailValue() {
        return patientEmail != null ? patientEmail.getValue() : null;
    }

    public String getPatientTelValue() {
        return patientTel != null ? patientTel.getValue() : null;
    }

    public String getPatientForeignValue() {
        return isForeign != null && isForeign ? "Y" : "N";
    }

    public String getPatientHypassYNValue() {
        return hasHypass != null && hasHypass ? "Y" : "N";
    }
}

