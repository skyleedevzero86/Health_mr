package com.sleekydz86.finance.payment.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends BaseRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<PaymentEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<PaymentEntity> findByPaymentStatus(PaymentStatus status);

    List<PaymentEntity> findByPaymentMethod(PaymentMethod method);

    List<PaymentEntity> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM Payment p WHERE DATE(p.paymentDate) = :date")
    List<PaymentEntity> findByPaymentDate(@Param("date") LocalDate date);

    Optional<PaymentEntity> findByTreatmentEntity_TreatmentIdAndPaymentStatus(Long treatmentId, PaymentStatus status);

    Page<PaymentEntity> findAll(Pageable pageable);

    Page<PaymentEntity> findByPatientEntity_PatientNo(Long patientNo, Pageable pageable);

    Page<PaymentEntity> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    Page<PaymentEntity> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<PaymentEntity> findAllByOrderByPaymentDateDesc();

    @Query("SELECT p FROM Payment p WHERE DATE(p.paymentDate) = CURRENT_DATE")
    List<PaymentEntity> findTodayPayments();

    @Query("SELECT COUNT(p), SUM(p.paymentTotalAmount), SUM(p.paymentCurrentMoney) " +
            "FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    Object[] getPaymentStatisticsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(p.paymentRemainMoney) FROM Payment p " +
            "WHERE p.patientEntity.patientNo = :patientNo AND p.paymentStatus IN :statuses")
    Long getUnpaidAmountByPatientNo(@Param("patientNo") Long patientNo,
                                    @Param("statuses") List<PaymentStatus> statuses);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Payment p WHERE p.treatmentEntity.treatmentId = :treatmentId " +
            "AND p.paymentStatus = :status")
    boolean existsByTreatmentIdAndStatus(@Param("treatmentId") Long treatmentId,
                                         @Param("status") PaymentStatus status);
}

