package com.sleekydz86.emrclinical.treatment.statistics.department.repository;

import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import java.util.List;

public interface TreatmentDepartmentStatisticsRepositoryCustom {
    List<TreatmentDepartmentStatisticsEntity> findByYearAndDepartmentAndRegion(
            String year,
            String departmentName,
            String regionCode
    );
}
