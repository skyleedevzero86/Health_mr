package com.sleekydz86.emrclinical.treatment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.emrclinical.treatment.entity.QTreatmentEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TreatmentRepositoryImpl implements TreatmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QTreatmentEntity treatment = QTreatmentEntity.treatmentEntity;

    public TreatmentRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<TreatmentEntity> findByConditions(
            Long patientNo,
            Long doctorId,
            Long departmentId,
            TreatmentType treatmentType,
            TreatmentStatus treatmentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        BooleanBuilder builder = buildConditions(
                patientNo, doctorId, departmentId, treatmentType, treatmentStatus, startDate, endDate
        );

        return queryFactory
                .selectFrom(treatment)
                .where(builder)
                .orderBy(treatment.treatmentDate.desc())
                .fetch();
    }

    @Override
    public Page<TreatmentEntity> findByConditionsWithPaging(
            Long patientNo,
            Long doctorId,
            Long departmentId,
            TreatmentType treatmentType,
            TreatmentStatus treatmentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        BooleanBuilder builder = buildConditions(
                patientNo, doctorId, departmentId, treatmentType, treatmentStatus, startDate, endDate
        );

        JPAQuery<TreatmentEntity> query = queryFactory
                .selectFrom(treatment)
                .where(builder)
                .orderBy(treatment.treatmentDate.desc());

        JPAQuery<Long> countQuery = queryFactory
                .select(treatment.count())
                .from(treatment)
                .where(builder);

        List<TreatmentEntity> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<TreatmentEntity> findByCheckInIds(List<Long> checkInIds) {
        if (checkInIds == null || checkInIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(treatment)
                .where(treatment.checkInEntity.checkInId.in(checkInIds))
                .fetch();
    }

    @Override
    public Long countByDoctorAndDateRange(Long doctorId, LocalDateTime start, LocalDateTime end) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(treatment.treatmentDoc.id.eq(doctorId));
        builder.and(treatment.treatmentDate.between(start, end));

        return queryFactory
                .select(treatment.count())
                .from(treatment)
                .where(builder)
                .fetchOne();
    }

    private BooleanBuilder buildConditions(
            Long patientNo,
            Long doctorId,
            Long departmentId,
            TreatmentType treatmentType,
            TreatmentStatus treatmentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (patientNo != null) {
            builder.and(treatment.patientEntity.patientNo.value.eq(patientNo));
        }
        if (doctorId != null) {
            builder.and(treatment.treatmentDoc.id.eq(doctorId));
        }
        if (departmentId != null) {
            builder.and(treatment.departmentEntity.departmentId.eq(departmentId));
        }
        if (treatmentType != null) {
            builder.and(treatment.treatmentType.eq(treatmentType));
        }
        if (treatmentStatus != null) {
            builder.and(treatment.treatmentStatus.eq(treatmentStatus));
        }
        if (startDate != null) {
            builder.and(treatment.treatmentDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(treatment.treatmentDate.loe(endDate));
        }

        return builder;
    }

    @Override
    public List<TreatmentEntity> findTodayTreatments(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return queryFactory
                .selectFrom(treatment)
                .where(treatment.treatmentDate.between(startOfDay, endOfDay))
                .orderBy(treatment.treatmentDate.desc())
                .fetch();
    }

    @Override
    public Long countByDateRange(LocalDateTime start, LocalDateTime end) {
        return queryFactory
                .select(treatment.count())
                .from(treatment)
                .where(treatment.treatmentDate.between(start, end))
                .fetchOne();
    }
}
