package com.sleekydz86.domain.institution.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Institution")
@Table(name = "institution")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstitutionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "institution_code", unique = true, nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String institutionCode;

    @Column(name = "institution_name", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String institutionName;

    @Column(name = "institution_eng_name", length = 100)
    @Size(max = 100)
    private String institutionEngName;

    @Column(name = "institution_address", length = 200)
    @Size(max = 200)
    private String institutionAddress;

    @Column(name = "institution_tel", length = 20)
    @Size(max = 20)
    private String institutionTel;

    @Column(name = "institution_email", length = 100)
    @Size(max = 100)
    private String institutionEmail;

    @Column(name = "director_name", length = 50)
    @Size(max = 50)
    private String directorName;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive;

    @Builder
    private InstitutionEntity(
            Long institutionId,
            String institutionCode,
            String institutionName,
            String institutionEngName,
            String institutionAddress,
            String institutionTel,
            String institutionEmail,
            String directorName,
            Boolean isActive) {
        validate(institutionCode, institutionName);
        this.institutionId = institutionId;
        this.institutionCode = institutionCode;
        this.institutionName = institutionName;
        this.institutionEngName = institutionEngName;
        this.institutionAddress = institutionAddress;
        this.institutionTel = institutionTel;
        this.institutionEmail = institutionEmail;
        this.directorName = directorName;
        this.isActive = isActive != null ? isActive : true;
    }

    private void validate(String institutionCode, String institutionName) {
        if (institutionCode == null || institutionCode.isBlank()) {
            throw new IllegalArgumentException("기관 코드는 필수입니다.");
        }
        if (institutionName == null || institutionName.isBlank()) {
            throw new IllegalArgumentException("기관명은 필수입니다.");
        }
    }

    public void update(
            String institutionName,
            String institutionEngName,
            String institutionAddress,
            String institutionTel,
            String institutionEmail,
            String directorName) {
        if (institutionName != null && !institutionName.isBlank()) {
            this.institutionName = institutionName;
        }
        if (institutionEngName != null) {
            this.institutionEngName = institutionEngName;
        }
        if (institutionAddress != null) {
            this.institutionAddress = institutionAddress;
        }
        if (institutionTel != null) {
            this.institutionTel = institutionTel;
        }
        if (institutionEmail != null) {
            this.institutionEmail = institutionEmail;
        }
        if (directorName != null) {
            this.directorName = directorName;
        }
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }
}
