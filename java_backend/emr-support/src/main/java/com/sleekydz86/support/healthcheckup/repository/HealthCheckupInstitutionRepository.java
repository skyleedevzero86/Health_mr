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
public interface HealthCheckupInstitutionRepository extends BaseRepository<HealthCheckupInstitutionEntity, Long>, HealthCheckupInstitutionRepositoryCustom {

    List<HealthCheckupInstitutionEntity> findByRegionCodeAndIsActive(String regionCode, Boolean isActive);

    List<HealthCheckupInstitutionEntity> findBySidoAndIsActive(String sido, Boolean isActive);

    List<HealthCheckupInstitutionEntity> findByInstitutionTypeAndIsActive(String institutionType, Boolean isActive);

}

