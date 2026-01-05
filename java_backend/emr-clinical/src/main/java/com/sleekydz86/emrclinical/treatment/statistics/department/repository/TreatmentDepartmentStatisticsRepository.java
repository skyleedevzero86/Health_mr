package com.sleekydz86.emrclinical.treatment.statistics.department.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentDepartmentStatisticsRepository extends BaseRepository<TreatmentDepartmentStatisticsEntity, Long> {

    List<TreatmentDepartmentStatisticsEntity> findByStatisticsYear(String year);

    List<TreatmentDepartmentStatisticsEntity> findByStatisticsYearAndDepartmentName(String year, String departmentName);

    List<TreatmentDepartmentStatisticsEntity> findByStatisticsYearAndRegionCode(String year, String regionCode);

    @Query("SELECT s FROM TreatmentDepartmentStatistics s WHERE " +
           "s.statisticsYear = :year AND " +
           "(:departmentName IS NULL OR s.departmentName = :departmentName) AND " +
           "(:regionCode IS NULL OR s.regionCode = :regionCode)")
    List<TreatmentDepartmentStatisticsEntity> findByYearAndDepartmentAndRegion(
            @Param("year") String year,
            @Param("departmentName") String departmentName,
            @Param("regionCode") String regionCode);
}

