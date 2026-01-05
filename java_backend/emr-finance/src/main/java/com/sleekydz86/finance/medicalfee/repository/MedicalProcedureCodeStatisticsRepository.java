package com.sleekydz86.finance.medicalfee.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.medicalfee.entity.MedicalProcedureCodeStatisticsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalProcedureCodeStatisticsRepository extends BaseRepository<MedicalProcedureCodeStatisticsEntity, Long> {

    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCode(String procedureCode);

    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndTreatmentYear(String procedureCode, String treatmentYear);

    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndInstitutionType(String procedureCode, String institutionType);

    @Query("SELECT s FROM MedicalProcedureCodeStatistics s WHERE " +
           "s.procedureCode = :procedureCode AND " +
           "(:startYear IS NULL OR s.treatmentYear >= :startYear) AND " +
           "(:endYear IS NULL OR s.treatmentYear <= :endYear) AND " +
           "(:institutionType IS NULL OR s.institutionType = :institutionType)")
    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndYearRange(
            @Param("procedureCode") String procedureCode,
            @Param("startYear") String startYear,
            @Param("endYear") String endYear,
            @Param("institutionType") String institutionType);
}

