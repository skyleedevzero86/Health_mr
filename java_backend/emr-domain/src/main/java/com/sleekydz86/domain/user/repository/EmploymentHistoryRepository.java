package com.sleekydz86.domain.user.repository;

import com.sleekydz86.domain.user.entity.EmploymentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistoryEntity, Long> {

    List<EmploymentHistoryEntity> findByUser_IdOrderByTenureSequenceDesc(Long userId);

    List<EmploymentHistoryEntity> findByUser_IdOrderByTenureSequenceAsc(Long userId);

    Optional<EmploymentHistoryEntity> findTopByUser_IdOrderByTenureSequenceDesc(Long userId);

    @Query("SELECT COALESCE(MAX(e.tenureSequence), 0) FROM EmploymentHistory e WHERE e.user.id = :userId")
    int findMaxTenureSequenceByUserId(@Param("userId") Long userId);

    boolean existsByInttCdAndEmployeeNo(String inttCd, String employeeNo);
}
