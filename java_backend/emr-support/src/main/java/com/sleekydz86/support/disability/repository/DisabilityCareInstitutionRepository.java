package com.sleekydz86.support.disability.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.disability.entity.DisabilityCareInstitutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisabilityCareInstitutionRepository extends BaseRepository<DisabilityCareInstitutionEntity, Long> {
    
    List<DisabilityCareInstitutionEntity> findByServiceTypeAndIsActive(String serviceType, Boolean isActive);
    
    List<DisabilityCareInstitutionEntity> findBySidoAndIsActive(String sido, Boolean isActive);
    
    List<DisabilityCareInstitutionEntity> findByInstitutionTypeAndIsActive(String institutionType, Boolean isActive);
    
    @Query("SELECT d FROM DisabilityCareInstitution d WHERE " +
           "(:serviceType IS NULL OR d.serviceType = :serviceType) AND " +
           "(:sido IS NULL OR d.sido = :sido) AND " +
           "(:institutionType IS NULL OR d.institutionType = :institutionType) AND " +
           "d.isActive = true")
    Page<DisabilityCareInstitutionEntity> searchInstitutions(
            @Param("serviceType") String serviceType,
            @Param("sido") String sido,
            @Param("institutionType") String institutionType,
            Pageable pageable);
    
    @Query("SELECT d FROM DisabilityCareInstitution d WHERE d.isActive = true")
    Page<DisabilityCareInstitutionEntity> findAllActive(Pageable pageable);
}

