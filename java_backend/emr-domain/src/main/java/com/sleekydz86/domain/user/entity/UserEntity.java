package com.sleekydz86.domain.user.entity;

import com.sleekydz86.core.event.domain.UserRoleChangedEvent;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.LoginId;
import com.sleekydz86.domain.common.valueobject.Password;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.user.type.Gender;
import com.sleekydz86.domain.user.type.AccountStatus;
import com.sleekydz86.domain.user.type.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Entity(name = "User")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_institution_employee", columnNames = {"intt_cd", "employee_no"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20)
    private AccountStatus accountStatus;

    @Column(name = "employee_no", length = 30)
    private String employeeNo;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "login_id", unique = true, nullable = false, length = 50))
    private LoginId loginId;

    @Embedded
    @AttributeOverride(name = "encryptedValue", column = @Column(name = "password", nullable = false))
    private Password password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Size(max = 200)
    private String address;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", length = 100))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "tel_num", length = 20))
    private PhoneNumber telNum;

    private LocalDateTime birth;

    private LocalDateTime hireDate;

    @Builder
    private UserEntity(
            Long id,
            RoleType role,
            AccountStatus accountStatus,
            String employeeNo,
            LoginId loginId,
            Password password,
            DepartmentEntity department,
            String name,
            Gender gender,
            String address,
            Email email,
            PhoneNumber telNum,
            LocalDateTime birth,
            LocalDateTime hireDate,
            String inttCd
    ) {
        this.id = id;
        this.role = role != null ? role : RoleType.WAIT;
        this.accountStatus = accountStatus != null
                ? accountStatus
                : (this.role == RoleType.WAIT ? AccountStatus.WAITING_APPROVAL : AccountStatus.ACTIVE);
        this.employeeNo = employeeNo != null ? employeeNo.trim() : null;
        this.loginId = loginId;
        this.password = password;
        this.department = department;
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.email = email;
        this.telNum = telNum;
        this.birth = birth;
        this.hireDate = hireDate;
        this.inttCd = inttCd;
    }

    public void changePassword(String newPlainPassword, PasswordEncoder passwordEncoder) {
        this.password = Password.fromPlainText(newPlainPassword, passwordEncoder);
    }

    public void changePassword(Password newPassword) {
        this.password = newPassword;
    }

    public boolean verifyPassword(String plainPassword, PasswordEncoder passwordEncoder) {
        return this.password.matches(plainPassword, passwordEncoder);
    }

    public void changeRole(RoleType newRole, EventPublisher eventPublisher) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 필수입니다.");
        }

        RoleType oldRole = this.role;

        if (this.role == RoleType.WAIT && newRole != RoleType.WAIT) {

            this.role = newRole;
            publishRoleChangedEvent(oldRole, newRole, eventPublisher);
        } else if (this.role != RoleType.WAIT) {

            this.role = newRole;
            publishRoleChangedEvent(oldRole, newRole, eventPublisher);
        }
    }

    public void approve(RoleType approvedRole, EventPublisher eventPublisher) {
        if (approvedRole == null || approvedRole == RoleType.WAIT) {
            throw new IllegalArgumentException("승인 시 실제 역할을 지정해야 합니다.");
        }
        changeRole(approvedRole, eventPublisher);
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void activate() {
        if (this.accountStatus == AccountStatus.RETIRED) {
            throw new IllegalStateException("퇴직 계정은 활성화할 수 없습니다.");
        }
        if (this.role == RoleType.WAIT) {
            throw new IllegalStateException("승인 대기 계정은 먼저 역할 승인이 필요합니다.");
        }
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void suspend() {
        if (this.accountStatus == AccountStatus.RETIRED) {
            throw new IllegalStateException("이미 퇴직 처리된 계정입니다.");
        }
        this.accountStatus = AccountStatus.SUSPENDED;
    }

    public void retire() {
        this.accountStatus = AccountStatus.RETIRED;
    }

    public void requestRehire(String newEmployeeNo, String newInstitutionCode,
                              DepartmentEntity newDepartment, LocalDateTime newHireDate) {
        if (this.accountStatus != AccountStatus.RETIRED) {
            throw new IllegalStateException("퇴직 계정만 재입사 처리할 수 있습니다.");
        }
        if (newEmployeeNo == null || newEmployeeNo.isBlank()) {
            throw new IllegalArgumentException("새 사원번호는 필수입니다.");
        }
        if (newInstitutionCode == null || newInstitutionCode.isBlank()) {
            throw new IllegalArgumentException("기관 코드는 필수입니다.");
        }
        if (newDepartment == null || newHireDate == null) {
            throw new IllegalArgumentException("부서와 재입사일은 필수입니다.");
        }
        this.employeeNo = newEmployeeNo.trim();
        this.inttCd = newInstitutionCode.trim();
        this.department = newDepartment;
        this.hireDate = newHireDate;
        this.role = RoleType.WAIT;
        this.accountStatus = AccountStatus.WAITING_APPROVAL;
    }

    public void changeRole(RoleType newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 필수입니다.");
        }
        this.role = newRole;
    }

    public void changeEmail(Email newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        this.email = newEmail;
    }

    public void changePhoneNumber(PhoneNumber newPhoneNumber) {
        if (newPhoneNumber == null) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        this.telNum = newPhoneNumber;
    }

    public void changeDepartment(DepartmentEntity newDepartment) {
        this.department = newDepartment;
    }

    public void updateProfile(String name, Gender gender, String address) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (address != null) {
            this.address = address;
        }
    }

    public boolean isWaitingApproval() {
        return this.accountStatus == AccountStatus.WAITING_APPROVAL;
    }

    public boolean isApproved() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public boolean canLogin() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    @PrePersist
    @PreUpdate
    @PostLoad
    private void normalizeAccountStatus() {
        if (this.accountStatus == null) {
            this.accountStatus = this.role == RoleType.WAIT
                    ? AccountStatus.WAITING_APPROVAL
                    : AccountStatus.ACTIVE;
        }
    }

    public boolean isAdmin() {
        return this.role == RoleType.ADMIN;
    }

    public boolean isDoctor() {
        return this.role == RoleType.DOCTOR;
    }


    public boolean isNurse() {
        return this.role == RoleType.NURSE;
    }

    private void publishRoleChangedEvent(RoleType oldRole, RoleType newRole, EventPublisher eventPublisher) {
        if (eventPublisher != null && oldRole != newRole) {
            eventPublisher.publish(new UserRoleChangedEvent(
                    this.id,
                    this.loginId.getValue(),
                    oldRole,
                    newRole
            ));
        }
    }

    public String getLoginIdValue() {
        return loginId != null ? loginId.getValue() : null;
    }

    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }

    public String getTelNumValue() {
        return telNum != null ? telNum.getValue() : null;
    }

    public String getPasswordValue() {
        return password != null ? password.getEncryptedValue() : null;
    }
}

