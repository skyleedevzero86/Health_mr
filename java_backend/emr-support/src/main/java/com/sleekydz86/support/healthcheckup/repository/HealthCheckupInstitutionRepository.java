package com.sleekydz86.support.healthcheckup.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.healthcheckup.entity.HealthCheckupInstitutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthCheckupInstitutionRepository extends BaseRepository<HealthCheckupInstitutionEntity, Long> {

    List<HealthCheckupInstitutionEntity> findByRegionCodeAndIsActive(String regionCode, Boolean isActive);

    List<HealthCheckupInstitutionEntity> findBySidoAndIsActive(String sido, Boolean isActive);

    List<HealthCheckupInstitutionEntity> findByInstitutionTypeAndIsActive(String institutionType, Boolean isActive);

    @Query("SELECT h FROM HealthCheckupInstitution h WHERE " +
           "(:regionCode IS NULL OR h.regionCode = :regionCode) AND " +
           "(:institutionType IS NULL OR h.institutionType = :institutionType) AND " +
           "(:institutionName IS NULL OR h.institutionName LIKE %:institutionName%) AND " +
           "(:sido IS NULL OR h.sido = :sido) AND " +
           "h.isActive = true")
    Page<HealthCheckupInstitutionEntity> searchInstitutions(
            @Param("regionCode") String regionCode,
            @Param("institutionType") String institutionType,
            @Param("institutionName") String institutionName,
            @Param("sido") String sido,
            Pageable pageable);

    @Query("SELECT h FROM HealthCheckupInstitution h WHERE h.isActive = true")
    Page<HealthCheckupInstitutionEntity> findAllActive(Pageable pageable);
}

