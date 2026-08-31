package com.sleekydz86.domain.user.entity;

import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.user.type.AccountStatus;
import com.sleekydz86.domain.user.type.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserEmploymentHistoryTest {

    @Test
    @DisplayName("고용 이력 생성 및 퇴직 처리 시 퇴직일과 상태가 정상 갱신된다")
    void createAndRetireEmploymentHistory() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .employeeNo("EMP-001")
                .name("홍길동")
                .role(RoleType.DOCTOR)
                .inttCd("HOSP001")
                .build();

        DepartmentEntity department = DepartmentEntity.builder()
                .id(10L)
                .name("내과")
                .build();

        LocalDateTime hireDate = LocalDateTime.of(2025, 1, 1, 9, 0);

        EmploymentHistoryEntity history = EmploymentHistoryEntity.builder()
                .user(user)
                .tenureSequence(1)
                .employeeNo("EMP-001")
                .inttCd("HOSP001")
                .department(department)
                .role(RoleType.DOCTOR)
                .employmentStatus(AccountStatus.ACTIVE)
                .hireDate(hireDate)
                .memo("최초 입사")
                .build();

        assertThat(history.getTenureSequence()).isEqualTo(1);
        assertThat(history.getEmployeeNo()).isEqualTo("EMP-001");
        assertThat(history.getEmploymentStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(history.getRetireDate()).isNull();

        LocalDateTime retireDate = LocalDateTime.of(2026, 3, 31, 18, 0);
        history.recordRetirement(retireDate, "개인 사유 퇴직");

        assertThat(history.getEmploymentStatus()).isEqualTo(AccountStatus.RETIRED);
        assertThat(history.getRetireDate()).isEqualTo(retireDate);
        assertThat(history.getMemo()).isEqualTo("개인 사유 퇴직");
    }

    @Test
    @DisplayName("재입사 시 이전 고용 이력과 분리되어 회차와 사원번호가 관리된다")
    void rehireEmploymentHistorySeparation() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .employeeNo("EMP-002")
                .name("이순신")
                .role(RoleType.NURSE)
                .inttCd("HOSP002")
                .build();

        DepartmentEntity firstDept = DepartmentEntity.builder().id(1L).name("응급실").build();
        DepartmentEntity secondDept = DepartmentEntity.builder().id(2L).name("중환자실").build();

        // 1회차 이력
        EmploymentHistoryEntity firstHistory = EmploymentHistoryEntity.builder()
                .user(user)
                .tenureSequence(1)
                .employeeNo("EMP-001")
                .inttCd("HOSP001")
                .department(firstDept)
                .role(RoleType.NURSE)
                .employmentStatus(AccountStatus.RETIRED)
                .hireDate(LocalDateTime.of(2023, 1, 1, 9, 0))
                .retireDate(LocalDateTime.of(2024, 12, 31, 18, 0))
                .memo("1회차 퇴직")
                .build();

        // 2회차 재입사 이력
        EmploymentHistoryEntity secondHistory = EmploymentHistoryEntity.builder()
                .user(user)
                .tenureSequence(2)
                .employeeNo("EMP-002")
                .inttCd("HOSP002")
                .department(secondDept)
                .role(RoleType.NURSE)
                .employmentStatus(AccountStatus.ACTIVE)
                .hireDate(LocalDateTime.of(2026, 8, 1, 9, 0))
                .memo("재입사 (2회차)")
                .build();

        assertThat(firstHistory.getTenureSequence()).isEqualTo(1);
        assertThat(firstHistory.getEmployeeNo()).isEqualTo("EMP-001");
        assertThat(firstHistory.getInttCd()).isEqualTo("HOSP001");

        assertThat(secondHistory.getTenureSequence()).isEqualTo(2);
        assertThat(secondHistory.getEmployeeNo()).isEqualTo("EMP-002");
        assertThat(secondHistory.getInttCd()).isEqualTo("HOSP002");
        assertThat(secondHistory.getDepartment().getName()).isEqualTo("중환자실");
    }
}
