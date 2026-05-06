package com.sleekydz86.support.healthcheckup.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.support.healthcheckup.entity.HealthCheckupInstitutionEntity;
import com.sleekydz86.support.healthcheckup.entity.QHealthCheckupInstitutionEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class HealthCheckupInstitutionRepositoryImpl implements HealthCheckupInstitutionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QHealthCheckupInstitutionEntity institution = QHealthCheckupInstitutionEntity.healthCheckupInstitutionEntity;

    public HealthCheckupInstitutionRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<HealthCheckupInstitutionEntity> searchInstitutions(
            String regionCode,
            String institutionType,
            String institutionName,
            String sido,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(institution.isActive.eq(true));

        if (StringUtils.hasText(regionCode)) {
            builder.and(institution.regionCode.eq(regionCode));
        }
        if (StringUtils.hasText(institutionType)) {
            builder.and(institution.institutionType.eq(institutionType));
        }
        if (StringUtils.hasText(institutionName)) {
            builder.and(institution.institutionName.containsIgnoreCase(institutionName));
        }
        if (StringUtils.hasText(sido)) {
            builder.and(institution.sido.eq(sido));
        }

        JPAQuery<HealthCheckupInstitutionEntity> query = queryFactory
                .selectFrom(institution)
                .where(builder)
                .orderBy(institution.institutionName.asc());

        JPAQuery<Long> countQuery = queryFactory
                .select(institution.count())
                .from(institution)
                .where(builder);

        List<HealthCheckupInstitutionEntity> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<HealthCheckupInstitutionEntity> findAllActive(Pageable pageable) {
        JPAQuery<HealthCheckupInstitutionEntity> query = queryFactory
                .selectFrom(institution)
                .where(institution.isActive.eq(true))
                .orderBy(institution.institutionName.asc());

        JPAQuery<Long> countQuery = queryFactory
                .select(institution.count())
                .from(institution)
                .where(institution.isActive.eq(true));

        List<HealthCheckupInstitutionEntity> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
