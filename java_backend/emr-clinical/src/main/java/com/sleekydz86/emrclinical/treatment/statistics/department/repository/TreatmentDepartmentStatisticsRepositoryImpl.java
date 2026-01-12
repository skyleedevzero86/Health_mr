package com.sleekydz86.emrclinical.treatment.statistics.department.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.emrclinical.treatment.statistics.department.entity.QTreatmentDepartmentStatisticsEntity;
import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class TreatmentDepartmentStatisticsRepositoryImpl implements TreatmentDepartmentStatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QTreatmentDepartmentStatisticsEntity statistics = QTreatmentDepartmentStatisticsEntity.treatmentDepartmentStatisticsEntity;

    public TreatmentDepartmentStatisticsRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<TreatmentDepartmentStatisticsEntity> findByYearAndDepartmentAndRegion(
            String year,
            String departmentName,
            String regionCode
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(statistics.statisticsYear.eq(year));

        if (StringUtils.hasText(departmentName)) {
            builder.and(statistics.departmentName.eq(departmentName));
        }
        if (StringUtils.hasText(regionCode)) {
            builder.and(statistics.regionCode.eq(regionCode));
        }

        return queryFactory
                .selectFrom(statistics)
                .where(builder)
                .orderBy(statistics.departmentName.asc())
                .fetch();
    }
}
