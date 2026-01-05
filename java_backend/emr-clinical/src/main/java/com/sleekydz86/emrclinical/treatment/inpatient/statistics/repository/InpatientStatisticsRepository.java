package com.sleekydz86.emrclinical.treatment.inpatient.statistics.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InpatientStatisticsRepository extends BaseRepository<InpatientStatisticsEntity, Long> {

    List<InpatientStatisticsEntity> findByStatisticsYear(String year);

    List<InpatientStatisticsEntity> findByStatisticsYearAndInstitutionType(String year, String institutionType);

    List<InpatientStatisticsEntity> findByStatisticsYearAndRegionCode(String year, String regionCode);

    @Query("SELECT s FROM InpatientStatistics s WHERE " +
           "s.statisticsYear = :year AND " +
           "(:institutionType IS NULL OR s.institutionType = :institutionType) AND " +
           "(:regionCode IS NULL OR s.regionCode = :regionCode)")
    List<InpatientStatisticsEntity> findByYearAndTypeAndRegion(
            @Param("year") String year,
            @Param("institutionType") String institutionType,
            @Param("regionCode") String regionCode);
}

