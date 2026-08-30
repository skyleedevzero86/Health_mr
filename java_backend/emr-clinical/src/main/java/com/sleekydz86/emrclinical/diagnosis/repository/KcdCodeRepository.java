package com.sleekydz86.emrclinical.diagnosis.repository;

import com.sleekydz86.emrclinical.diagnosis.entity.KcdCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface KcdCodeRepository extends JpaRepository<KcdCodeEntity, Long> {

    Optional<KcdCodeEntity> findByCode(String code);

    @Query("SELECT k FROM KcdCodeEntity k WHERE k.active = true " +
            "AND (LOWER(k.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(k.nameKorean) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(k.nameEnglish) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KcdCodeEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<KcdCodeEntity> findByCategory(String category, Pageable pageable);

    boolean existsByCode(String code);
}
