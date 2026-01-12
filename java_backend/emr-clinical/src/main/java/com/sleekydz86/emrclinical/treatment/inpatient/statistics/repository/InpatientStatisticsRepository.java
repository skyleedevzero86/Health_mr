package com.sleekydz86.emrclinical.treatment.inpatient.statistics.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InpatientStatisticsRepository extends BaseRepository<InpatientStatisticsEntity, Long>, InpatientStatisticsRepositoryCustom {

    List<InpatientStatisticsEntity> findByStatisticsYear(String year);

    List<InpatientStatisticsEntity> findByStatisticsYearAndInstitutionType(String year, String institutionType);

    List<InpatientStatisticsEntity> findByStatisticsYearAndRegionCode(String year, String regionCode);

}

