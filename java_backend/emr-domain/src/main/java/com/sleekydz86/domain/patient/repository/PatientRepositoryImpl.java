package com.sleekydz86.domain.patient.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.entity.QPatientEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class PatientRepositoryImpl implements PatientRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPatientEntity patient = QPatientEntity.patientEntity;

    public PatientRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<PatientEntity> searchPatients(String name, String telHmac, String emailHmac) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(name)) {
            builder.and(patient.patientName.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(telHmac)) {
            builder.and(patient.telHmac.eq(telHmac));
        }
        if (StringUtils.hasText(emailHmac)) {
            builder.and(patient.emailHmac.eq(emailHmac));
        }

        return queryFactory
                .selectFrom(patient)
                .where(builder)
                .fetch();
    }

    @Override
    public Page<PatientEntity> searchPatientsWithPaging(String name, String telHmac, String emailHmac, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(name)) {
            builder.and(patient.patientName.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(telHmac)) {
            builder.and(patient.telHmac.eq(telHmac));
        }
        if (StringUtils.hasText(emailHmac)) {
            builder.and(patient.emailHmac.eq(emailHmac));
        }

        JPAQuery<PatientEntity> query = queryFactory
                .selectFrom(patient)
                .where(builder);

        JPAQuery<Long> countQuery = queryFactory
                .select(patient.count())
                .from(patient)
                .where(builder);

        List<PatientEntity> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
