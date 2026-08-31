package com.sleekydz86.domain.user.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
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

import java.time.LocalDateTime;

@Entity(name = "EmploymentHistory")
@Table(
        name = "employment_history",
        indexes = {
                @Index(name = "idx_emp_hist_user_seq", columnList = "user_id, tenure_sequence"),
                @Index(name = "idx_emp_hist_intt_emp_no", columnList = "intt_cd, employee_no")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmploymentHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private UserEntity user;

    @Column(name = "tenure_sequence", nullable = false)
    @NotNull
    private Integer tenureSequence;

    @Column(name = "employee_no", length = 30, nullable = false)
    @NotBlank
    @Size(max = 30)
    private String employeeNo;

    @Column(name = "intt_cd", length = 20, nullable = false)
    @NotBlank
    @Size(max = 20)
    private String inttCd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @NotNull
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 20, nullable = false)
    @NotNull
    private AccountStatus employmentStatus;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "retire_date")
    private LocalDateTime retireDate;

    @Column(name = "memo", length = 255)
    private String memo;

    @Builder
    public EmploymentHistoryEntity(
            Long id,
            UserEntity user,
            Integer tenureSequence,
            String employeeNo,
            String inttCd,
            DepartmentEntity department,
            RoleType role,
            AccountStatus employmentStatus,
            LocalDateTime hireDate,
            LocalDateTime retireDate,
            String memo
    ) {
        this.id = id;
        this.user = user;
        this.tenureSequence = tenureSequence != null ? tenureSequence : 1;
        this.employeeNo = employeeNo != null ? employeeNo.trim() : null;
        this.inttCd = inttCd != null ? inttCd.trim() : null;
        this.department = department;
        this.role = role != null ? role : RoleType.WAIT;
        this.employmentStatus = employmentStatus != null ? employmentStatus : AccountStatus.ACTIVE;
        this.hireDate = hireDate;
        this.retireDate = retireDate;
        this.memo = memo;
    }

    public void recordRetirement(LocalDateTime retireDate, String memo) {
        this.retireDate = retireDate != null ? retireDate : LocalDateTime.now();
        this.employmentStatus = AccountStatus.RETIRED;
        if (memo != null && !memo.isBlank()) {
            this.memo = memo;
        }
    }

    public void updateRole(RoleType role) {
        if (role != null) {
            this.role = role;
        }
    }

    public void updateDepartment(DepartmentEntity department) {
        this.department = department;
    }

    public void updateStatus(AccountStatus status) {
        if (status != null) {
            this.employmentStatus = status;
        }
    }
}
