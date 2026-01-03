package com.sleekydz86.finance.medicalfee.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalFeeRepository extends BaseRepository<MedicalFeeEntity, Long> {

    List<MedicalFeeEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<MedicalFeeEntity> findByMedicalTypeEntity_MedicalTypeId(Long medicalTypeId);

    Optional<MedicalFeeEntity> findByTreatmentEntity_TreatmentIdAndMedicalTypeEntity_MedicalTypeId(
            Long treatmentId, Long medicalTypeId);

    Page<MedicalFeeEntity> findAll(Pageable pageable);

    Page<MedicalFeeEntity> findByTreatmentEntity_TreatmentId(Long treatmentId, Pageable pageable);

    List<MedicalFeeEntity> findAllByOrderByCreatedDateDesc();

    @Query("SELECT COALESCE(SUM(mf.medicalFeeAmount * mf.quantity), 0) " +
            "FROM MedicalFeeEntity mf WHERE mf.treatmentEntity.treatmentId = :treatmentId")
    Long getTotalMedicalFeeByTreatmentId(@Param("treatmentId") Long treatmentId);

    @Query("SELECT mf.medicalTypeEntity.medicalTypeId, mf.medicalTypeEntity.medicalTypeName, " +
            "COUNT(mf), SUM(mf.medicalFeeAmount * mf.quantity) " +
            "FROM MedicalFeeEntity mf GROUP BY mf.medicalTypeEntity.medicalTypeId, mf.medicalTypeEntity.medicalTypeName")
    List<Object[]> getMedicalFeeStatisticsByType();
}

