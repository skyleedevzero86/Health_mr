package com.sleekydz86.core.audit.repository;

import com.sleekydz86.core.audit.entity.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface AuditRepository extends JpaRepository<AuditEntity, Long> {

    @Query("SELECT a FROM AuditEntity a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    Page<AuditEntity> findByUserId(Long userId, Pageable pageable);
    Page<AuditEntity> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);
    Page<AuditEntity> findByActionType(String actionType, Pageable pageable);
}

