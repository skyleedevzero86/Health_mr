package com.sleekydz86.finance.payment.service;

import com.sleekydz86.finance.payment.dto.DailyPaymentStatistics;
import com.sleekydz86.finance.payment.dto.PeriodPaymentStatistics;
import com.sleekydz86.finance.payment.dto.UnpaidPaymentStatistics;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentStatisticsService {

    private final PaymentRepository paymentRepository;

    public DailyPaymentStatistics getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<PaymentEntity> payments =
                paymentRepository.findByPaymentDateBetween(start, end);

        long count = payments.size();
        long totalAmount = payments.stream()
                .mapToLong(p -> p.getPaymentTotalAmount() != null ? p.getPaymentTotalAmount() : 0L)
                .sum();
        long paidAmount = payments.stream()
                .mapToLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                .sum();

        Map<PaymentMethod, Long> methodStatistics = payments.stream()
                .filter(p -> p.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        PaymentEntity::getPaymentMethod,
                        Collectors.summingLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                ));

        Map<PaymentStatus, Long> statusStatistics = payments.stream()
                .filter(p -> p.getPaymentStatus() != null)
                .collect(Collectors.groupingBy(
                        PaymentEntity::getPaymentStatus,
                        Collectors.counting()
                ));

        return DailyPaymentStatistics.builder()
                .date(date)
                .count(count)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .methodStatistics(methodStatistics)
                .statusStatistics(statusStatistics)
                .build();
    }

    public PeriodPaymentStatistics getPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<PaymentEntity> payments =
                paymentRepository.findByPaymentDateBetween(start, end);

        long count = payments.size();
        long totalAmount = payments.stream()
                .mapToLong(p -> p.getPaymentTotalAmount() != null ? p.getPaymentTotalAmount() : 0L)
                .sum();
        long paidAmount = payments.stream()
                .mapToLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                .sum();

        Map<PaymentMethod, Long> methodStatistics = payments.stream()
                .filter(p -> p.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        PaymentEntity::getPaymentMethod,
                        Collectors.summingLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                ));


        Map<PaymentStatus, Long> statusStatistics = payments.stream()
                .filter(p -> p.getPaymentStatus() != null)
                .collect(Collectors.groupingBy(
                        PaymentEntity::getPaymentStatus,
                        Collectors.counting()
                ));


        Map<Long, Long> patientStatistics = payments.stream()
                .filter(p -> p.getPatientEntity() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getPatientEntity().getPatientNo(),
                        Collectors.summingLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                ));

        return PeriodPaymentStatistics.builder()
                .startDate(startDate)
                .endDate(endDate)
                .count(count)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .methodStatistics(methodStatistics)
                .statusStatistics(statusStatistics)
                .patientStatistics(patientStatistics)
                .build();
    }

    public UnpaidPaymentStatistics getUnpaidStatistics() {
        List<PaymentEntity> unpaidPayments =
                paymentRepository.findByPaymentStatus(PaymentStatus.UNPAID);

        unpaidPayments.addAll(paymentRepository.findByPaymentStatus(PaymentStatus.PARTIAL));

        long count = unpaidPayments.size();
        long totalUnpaidAmount = unpaidPayments.stream()
                .mapToLong(p -> p.getPaymentRemainMoney() != null ? p.getPaymentRemainMoney() : 0L)
                .sum();


        Map<Long, Long> patientStatistics = unpaidPayments.stream()
                .filter(p -> p.getPatientEntity() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getPatientEntity().getPatientNo(),
                        Collectors.summingLong(p -> p.getPaymentRemainMoney() != null ? p.getPaymentRemainMoney() : 0L)
                ));

        return UnpaidPaymentStatistics.builder()
                .count(count)
                .totalUnpaidAmount(totalUnpaidAmount)
                .patientStatistics(patientStatistics)
                .build();
    }
}

