package com.sleekydz86.domain.user.entity;

import com.sleekydz86.domain.institution.entity.InstitutionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sleekydz86.domain.common.entity.BaseEntity;

@Entity(name = "UserInstitution")
@Table(name = "user_institution",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "institution_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInstitution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_institution_id", nullable = false)
    private Long userInstitutionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    @NotNull
    private InstitutionEntity institution;

    @Column(name = "is_primary", nullable = false)
    @NotNull
    private Boolean isPrimary;

    @Builder
    private UserInstitution(
            Long userInstitutionId,
            UserEntity user,
            InstitutionEntity institution,
            Boolean isPrimary
    ) {
        validate(user, institution);
        this.userInstitutionId = userInstitutionId;
        this.user = user;
        this.institution = institution;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    private void validate(UserEntity user, InstitutionEntity institution) {
        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다.");
        }
        if (institution == null) {
            throw new IllegalArgumentException("병원은 필수입니다.");
        }
    }

    public void setPrimary() {
        this.isPrimary = true;
    }

    public void unsetPrimary() {
        this.isPrimary = false;
    }

    public boolean isPrimary() {
        return Boolean.TRUE.equals(isPrimary);
    }

    public String getInstitutionCode() {
        return institution != null ? institution.getInstitutionCode() : null;
    }
}

