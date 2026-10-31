package com.sleekydz86.domain.user.entity;

import com.sleekydz86.domain.user.type.AccountStatus;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

class UserEntityAccountStatusTest {

    @Test
    @DisplayName("승인 시 계정 상태와 역할이 분리되어 갱신된다")
    void approvalSeparatesAccountStatusFromRole() {
        // given
        UserEntity user = UserEntity.builder()
                .employeeNo("E-1001")
                .role(RoleType.WAIT)
                .accountStatus(AccountStatus.WAITING_APPROVAL)
                .name("승인대기자")
                .build();
        assertThat(user.canLogin()).isFalse();

        // when
        user.approve(RoleType.DOCTOR, null);

        // then
        assertThat(user.getRole()).isEqualTo(RoleType.DOCTOR);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.canLogin()).isTrue();
    }

    @Test
    @DisplayName("퇴직 계정은 다시 활성화할 수 없다")
    void retiredAccountCannotBeActivatedAgain() {
        // given
        UserEntity user = UserEntity.builder()
                .employeeNo("E-1002")
                .role(RoleType.NURSE)
                .accountStatus(AccountStatus.ACTIVE)
                .name("퇴직자")
                .build();

        // when
        user.retire();

        // then
        assertThat(user.canLogin()).isFalse();
        assertThatThrownBy(user::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("퇴직 계정");
    }

    @Test
    @DisplayName("재입사 요청 시 퇴직 계정은 다시 승인 대기 상태가 된다")
    void rehireReturnsRetiredAccountToApprovalFlow() {
        // given
        UserEntity user = UserEntity.builder()
                .employeeNo("OLD-100")
                .role(RoleType.DOCTOR)
                .accountStatus(AccountStatus.RETIRED)
                .name("재입사자")
                .build();
        DepartmentEntity newDepartment = DepartmentEntity.builder().id(3L).name("내과").code("IM").build();
        LocalDateTime rehireDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        // when
        user.requestRehire("NEW-200", "HOSP002", newDepartment, rehireDate);

        // then
        assertThat(user.getEmployeeNo()).isEqualTo("NEW-200");
        assertThat(user.getInttCd()).isEqualTo("HOSP002");
        assertThat(user.getDepartment()).isSameAs(newDepartment);
        assertThat(user.getHireDate()).isEqualTo(rehireDate);
        assertThat(user.getRole()).isEqualTo(RoleType.WAIT);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.WAITING_APPROVAL);
        assertThat(user.canLogin()).isFalse();
    }
}
