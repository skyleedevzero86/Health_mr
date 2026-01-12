package com.sleekydz86.support.healthcheckup.repository;

import com.sleekydz86.support.healthcheckup.entity.HealthCheckupInstitutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface HealthCheckupInstitutionRepositoryCustom {
    Page<HealthCheckupInstitutionEntity> searchInstitutions(
            String regionCode,
            String institutionType,
            String institutionName,
            String sido,
            Pageable pageable
    );
    Page<HealthCheckupInstitutionEntity> findAllActive(Pageable pageable);
}
