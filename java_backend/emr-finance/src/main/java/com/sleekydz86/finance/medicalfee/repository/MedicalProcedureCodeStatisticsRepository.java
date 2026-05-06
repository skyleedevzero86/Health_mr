package com.sleekydz86.finance.medicalfee.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.medicalfee.entity.MedicalProcedureCodeStatisticsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalProcedureCodeStatisticsRepository extends BaseRepository<MedicalProcedureCodeStatisticsEntity, Long>, MedicalProcedureCodeStatisticsRepositoryCustom {

    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCode(String procedureCode);

    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndTreatmentYear(String procedureCode, String treatmentYear);

    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndInstitutionType(String procedureCode, String institutionType);

}

