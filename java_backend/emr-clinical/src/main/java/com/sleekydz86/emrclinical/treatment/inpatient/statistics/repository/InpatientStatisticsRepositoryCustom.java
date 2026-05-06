package com.sleekydz86.emrclinical.treatment.inpatient.statistics.repository;

import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import java.util.List;

public interface InpatientStatisticsRepositoryCustom {
    List<InpatientStatisticsEntity> findByYearAndTypeAndRegion(
            String year,
            String institutionType,
            String regionCode
    );
}
