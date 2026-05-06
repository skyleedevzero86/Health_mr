package com.sleekydz86.finance.medicalfee.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.finance.medicalfee.entity.MedicalProcedureCodeStatisticsEntity;
import com.sleekydz86.finance.medicalfee.entity.QMedicalProcedureCodeStatisticsEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class MedicalProcedureCodeStatisticsRepositoryImpl implements MedicalProcedureCodeStatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QMedicalProcedureCodeStatisticsEntity statistics = QMedicalProcedureCodeStatisticsEntity.medicalProcedureCodeStatisticsEntity;

    public MedicalProcedureCodeStatisticsRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<MedicalProcedureCodeStatisticsEntity> findByProcedureCodeAndYearRange(
            String procedureCode,
            String startYear,
            String endYear,
            String institutionType
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(statistics.procedureCode.eq(procedureCode));

        if (StringUtils.hasText(startYear)) {
            builder.and(statistics.treatmentYear.goe(startYear));
        }
        if (StringUtils.hasText(endYear)) {
            builder.and(statistics.treatmentYear.loe(endYear));
        }
        if (StringUtils.hasText(institutionType)) {
            builder.and(statistics.institutionType.eq(institutionType));
        }

        return queryFactory
                .selectFrom(statistics)
                .where(builder)
                .orderBy(statistics.treatmentYear.asc())
                .fetch();
    }
}
