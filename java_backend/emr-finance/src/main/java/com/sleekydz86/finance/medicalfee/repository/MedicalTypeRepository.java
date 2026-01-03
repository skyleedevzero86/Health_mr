package com.sleekydz86.finance.medicalfee.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.medicalfee.entity.MedicalTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface MedicalTypeRepository extends BaseRepository<MedicalTypeEntity, Long> {

    Optional<MedicalTypeEntity> findByMedicalTypeCode(String code);

    boolean existsByMedicalTypeCode(String code);

    Optional<MedicalTypeEntity> findByMedicalTypeName(String name);

    boolean existsByMedicalTypeName(String name);

    List<MedicalTypeEntity> findByIsActive(Boolean isActive);

    Page<MedicalTypeEntity> findAll(Pageable pageable);

    Page<MedicalTypeEntity> findByIsActive(Boolean isActive, Pageable pageable);

    List<MedicalTypeEntity> findAllByOrderByMedicalTypeNameAsc();

    @Query("SELECT mt FROM MedicalTypeEntity mt " +
            "WHERE mt.isActive = true AND " +
            "(mt.medicalTypeName LIKE %:keyword% OR mt.medicalTypeCode LIKE %:keyword%)")
    List<MedicalTypeEntity> searchMedicalTypes(@Param("keyword") String keyword);
}

