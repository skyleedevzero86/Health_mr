package com.sleekydz86.emrclinical.treatment.statistics.department.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentDepartmentStatisticsRepository extends BaseRepository<TreatmentDepartmentStatisticsEntity, Long>, TreatmentDepartmentStatisticsRepositoryCustom {

    List<TreatmentDepartmentStatisticsEntity> findByStatisticsYear(String year);

    List<TreatmentDepartmentStatisticsEntity> findByStatisticsYearAndDepartmentName(String year, String departmentName);

    List<TreatmentDepartmentStatisticsEntity> findByStatisticsYearAndRegionCode(String year, String regionCode);

}

