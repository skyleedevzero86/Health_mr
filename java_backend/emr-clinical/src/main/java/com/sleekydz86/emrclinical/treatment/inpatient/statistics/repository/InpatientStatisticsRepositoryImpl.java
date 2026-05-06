package com.sleekydz86.emrclinical.treatment.inpatient.statistics.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.QInpatientStatisticsEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class InpatientStatisticsRepositoryImpl implements InpatientStatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInpatientStatisticsEntity statistics = QInpatientStatisticsEntity.inpatientStatisticsEntity;

    public InpatientStatisticsRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<InpatientStatisticsEntity> findByYearAndTypeAndRegion(
            String year,
            String institutionType,
            String regionCode
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(statistics.statisticsYear.eq(year));

        if (StringUtils.hasText(institutionType)) {
            builder.and(statistics.institutionType.eq(institutionType));
        }
        if (StringUtils.hasText(regionCode)) {
            builder.and(statistics.regionCode.eq(regionCode));
        }

        return queryFactory
                .selectFrom(statistics)
                .where(builder)
                .orderBy(statistics.regionCode.asc())
                .fetch();
    }
}
