package com.sleekydz86.finance.medicalfee.repository;

import com.sleekydz86.finance.medicalfee.entity.MedicalProcedureCodeStatisticsEntity;
import java.util.List;

public interface MedicalProcedureCodeStatisticsRepositoryCustom {
    List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndYearRange(
            String procedureCode,
            String startYear,
            String endYear,
            String institutionType
    );
}
