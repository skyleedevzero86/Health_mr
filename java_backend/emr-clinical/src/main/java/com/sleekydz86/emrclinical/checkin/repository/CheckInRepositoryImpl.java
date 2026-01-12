package com.sleekydz86.emrclinical.checkin.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.emrclinical.checkin.entity.QCheckInEntity;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CheckInRepositoryImpl implements CheckInRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QCheckInEntity checkIn = QCheckInEntity.checkInEntity;

    public CheckInRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<CheckInEntity> findByConditions(
            Long patientNo,
            Long userId,
            CheckInStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        BooleanBuilder builder = buildConditions(patientNo, userId, status, startDate, endDate);

        return queryFactory
                .selectFrom(checkIn)
                .where(builder)
                .orderBy(checkIn.checkInDate.desc())
                .fetch();
    }

    @Override
    public Page<CheckInEntity> findByConditionsWithPaging(
            Long patientNo,
            Long userId,
            CheckInStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        BooleanBuilder builder = buildConditions(patientNo, userId, status, startDate, endDate);

        JPAQuery<CheckInEntity> query = queryFactory
                .selectFrom(checkIn)
                .where(builder)
                .orderBy(checkIn.checkInDate.desc());

        JPAQuery<Long> countQuery = queryFactory
                .select(checkIn.count())
                .from(checkIn)
                .where(builder);

        List<CheckInEntity> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<CheckInEntity> findTodayCheckIns(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return queryFactory
                .selectFrom(checkIn)
                .where(checkIn.checkInDate.between(startOfDay, endOfDay))
                .orderBy(checkIn.checkInDate.desc())
                .fetch();
    }

    private BooleanBuilder buildConditions(
            Long patientNo,
            Long userId,
            CheckInStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (patientNo != null) {
            builder.and(checkIn.patientEntity.patientNo.value.eq(patientNo));
        }
        if (userId != null) {
            builder.and(checkIn.userEntity.id.eq(userId));
        }
        if (status != null) {
            builder.and(checkIn.checkInStatus.eq(status));
        }
        if (startDate != null) {
            builder.and(checkIn.checkInDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(checkIn.checkInDate.loe(endDate));
        }

        return builder;
    }

    @Override
    public List<CheckInEntity> findByPatientNo(Long patientNo) {
        return queryFactory
                .selectFrom(checkIn)
                .where(checkIn.patientEntity.patientNo.value.eq(patientNo))
                .orderBy(checkIn.checkInDate.desc())
                .fetch();
    }

    @Override
    public List<CheckInEntity> findByCheckInDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(checkIn)
                .where(checkIn.checkInDate.between(startDate, endDate))
                .orderBy(checkIn.checkInDate.desc())
                .fetch();
    }

    @Override
    public List<CheckInEntity> findByCheckInStatus(CheckInStatus status) {
        return queryFactory
                .selectFrom(checkIn)
                .where(checkIn.checkInStatus.eq(status))
                .orderBy(checkIn.checkInDate.desc())
                .fetch();
    }
}
