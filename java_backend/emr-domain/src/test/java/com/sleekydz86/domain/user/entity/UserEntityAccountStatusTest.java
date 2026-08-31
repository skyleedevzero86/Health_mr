package com.sleekydz86.domain.user.entity;

import com.sleekydz86.domain.user.type.AccountStatus;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

class UserEntityAccountStatusTest {
    @Test
    void approvalSeparatesAccountStatusFromRole() {
        UserEntity user = UserEntity.builder()
                .employeeNo("E-1001")
                .role(RoleType.WAIT)
                .accountStatus(AccountStatus.WAITING_APPROVAL)
                .name("승인대기자")
                .build();

        assertThat(user.canLogin()).isFalse();

        user.approve(RoleType.DOCTOR, null);

        assertThat(user.getRole()).isEqualTo(RoleType.DOCTOR);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.canLogin()).isTrue();
    }

    @Test
    void retiredAccountCannotBeActivatedAgain() {
        UserEntity user = UserEntity.builder()
                .employeeNo("E-1002")
                .role(RoleType.NURSE)
                .accountStatus(AccountStatus.ACTIVE)
                .name("퇴직자")
                .build();

        user.retire();

        assertThat(user.canLogin()).isFalse();
        assertThatThrownBy(user::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("퇴직 계정");
    }

    @Test
    void rehireReturnsRetiredAccountToApprovalFlow() {
        UserEntity user = UserEntity.builder()
                .employeeNo("OLD-100")
                .role(RoleType.DOCTOR)
                .accountStatus(AccountStatus.RETIRED)
                .name("재입사자")
                .build();
        DepartmentEntity newDepartment = DepartmentEntity.builder().id(3L).name("내과").code("IM").build();
        LocalDateTime rehireDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        user.requestRehire("NEW-200", "HOSP002", newDepartment, rehireDate);

        assertThat(user.getEmployeeNo()).isEqualTo("NEW-200");
        assertThat(user.getInttCd()).isEqualTo("HOSP002");
        assertThat(user.getDepartment()).isSameAs(newDepartment);
        assertThat(user.getHireDate()).isEqualTo(rehireDate);
        assertThat(user.getRole()).isEqualTo(RoleType.WAIT);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.WAITING_APPROVAL);
        assertThat(user.canLogin()).isFalse();
    }
}
