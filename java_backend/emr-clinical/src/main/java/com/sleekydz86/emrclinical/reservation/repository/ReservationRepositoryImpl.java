package com.sleekydz86.emrclinical.reservation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sleekydz86.emrclinical.reservation.entity.QReservationEntity;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReservationEntity reservation = QReservationEntity.reservationEntity;

    public ReservationRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<ReservationEntity> findByConditions(
            Long patientNo,
            Long userId,
            ReservationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        BooleanBuilder builder = buildConditions(patientNo, userId, status, startDate, endDate);

        return queryFactory
                .selectFrom(reservation)
                .where(builder)
                .orderBy(reservation.reservationDate.asc())
                .fetch();
    }

    @Override
    public Page<ReservationEntity> findByConditionsWithPaging(
            Long patientNo,
            Long userId,
            ReservationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        BooleanBuilder builder = buildConditions(patientNo, userId, status, startDate, endDate);

        JPAQuery<ReservationEntity> query = queryFactory
                .selectFrom(reservation)
                .where(builder)
                .orderBy(reservation.reservationDate.asc());

        JPAQuery<Long> countQuery = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(builder);

        List<ReservationEntity> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<ReservationEntity> findTodayReservations(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return queryFactory
                .selectFrom(reservation)
                .where(reservation.reservationDate.between(startOfDay, endOfDay))
                .orderBy(reservation.reservationDate.asc())
                .fetch();
    }

    @Override
    public Long countByDateRange(LocalDateTime start, LocalDateTime end) {
        return queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(reservation.reservationDate.between(start, end))
                .fetchOne();
    }

    private BooleanBuilder buildConditions(
            Long patientNo,
            Long userId,
            ReservationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (patientNo != null) {
            builder.and(reservation.patientEntity.patientNo.value.eq(patientNo));
        }
        if (userId != null) {
            builder.and(reservation.userEntity.id.eq(userId));
        }
        if (status != null) {
            builder.and(reservation.reservationStatus.eq(status));
        }
        if (startDate != null) {
            builder.and(reservation.reservationDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(reservation.reservationDate.loe(endDate));
        }

        return builder;
    }
}
