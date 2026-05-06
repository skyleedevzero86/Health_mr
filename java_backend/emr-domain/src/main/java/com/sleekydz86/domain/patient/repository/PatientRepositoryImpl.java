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
    public List<PatientEntity> searchPatients(String name, String tel, String email) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(name)) {
            builder.and(patient.patientName.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(tel)) {
            builder.and(patient.patientTel.value.containsIgnoreCase(tel));
        }
        if (StringUtils.hasText(email)) {
            builder.and(patient.patientEmail.value.containsIgnoreCase(email));
        }

        return queryFactory
                .selectFrom(patient)
                .where(builder)
                .fetch();
    }

    @Override
    public Page<PatientEntity> searchPatientsWithPaging(String name, String tel, String email, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(name)) {
            builder.and(patient.patientName.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(tel)) {
            builder.and(patient.patientTel.value.containsIgnoreCase(tel));
        }
        if (StringUtils.hasText(email)) {
            builder.and(patient.patientEmail.value.containsIgnoreCase(email));
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
