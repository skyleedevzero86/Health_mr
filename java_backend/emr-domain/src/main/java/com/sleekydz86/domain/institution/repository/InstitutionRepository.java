package com.sleekydz86.domain.institution.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.institution.entity.InstitutionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends BaseRepository<InstitutionEntity, Long> {


    @Query("SELECT i FROM Institution i WHERE i.institutionCode = :code")
    Optional<InstitutionEntity> findByInstitutionCode(@Param("code") String code);

    @Query("SELECT COUNT(i) > 0 FROM Institution i WHERE i.institutionCode = :code")
    boolean existsByInstitutionCode(@Param("code") String code);

    @Query("SELECT i FROM Institution i WHERE i.institutionCode = :code AND i.isActive = true")
    Optional<InstitutionEntity> findActiveByInstitutionCode(@Param("code") String code);
}
