package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.finance.medicalfee.dto.DailyMedicalFeeStatistics;
import com.sleekydz86.finance.medicalfee.dto.PeriodMedicalFeeStatistics;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import com.sleekydz86.finance.medicalfee.repository.MedicalFeeRepository;
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
public class MedicalFeeStatisticsService {

    private final MedicalFeeRepository medicalFeeRepository;

    public DailyMedicalFeeStatistics getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<MedicalFeeEntity> medicalFees = medicalFeeRepository.findAll().stream()
                .filter(mf -> mf.getCreatedAt() != null &&
                        mf.getCreatedAt().isAfter(start) &&
                        mf.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        long count = medicalFees.size();
        long totalAmount = medicalFees.stream()
                .mapToLong(mf -> {
                    Long amount = mf.getMedicalFeeAmount() != null ? mf.getMedicalFeeAmount() : 0L;
                    Integer quantity = mf.getQuantity() != null ? mf.getQuantity() : 1;
                    return amount * quantity;
                })
                .sum();

        Map<String, Long> typeStatistics = medicalFees.stream()
                .filter(mf -> mf.getMedicalTypeEntity() != null)
                .collect(Collectors.groupingBy(
                        mf -> mf.getMedicalTypeEntity().getMedicalTypeName(),
                        Collectors.summingLong(mf -> {
                            Long amount = mf.getMedicalFeeAmount() != null ? mf.getMedicalFeeAmount() : 0L;
                            Integer quantity = mf.getQuantity() != null ? mf.getQuantity() : 1;
                            return amount * quantity;
                        })
                ));

        return DailyMedicalFeeStatistics.builder()
                .date(date)
                .count(count)
                .totalAmount(totalAmount)
                .typeStatistics(typeStatistics)
                .build();
    }

    public PeriodMedicalFeeStatistics getPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<MedicalFeeEntity> medicalFees = medicalFeeRepository.findAll().stream()
                .filter(mf -> mf.getCreatedAt() != null &&
                        mf.getCreatedAt().isAfter(start) &&
                        mf.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        long count = medicalFees.size();
        long totalAmount = medicalFees.stream()
                .mapToLong(mf -> {
                    Long amount = mf.getMedicalFeeAmount() != null ? mf.getMedicalFeeAmount() : 0L;
                    Integer quantity = mf.getQuantity() != null ? mf.getQuantity() : 1;
                    return amount * quantity;
                })
                .sum();

        Map<String, Long> typeStatistics = medicalFees.stream()
                .filter(mf -> mf.getMedicalTypeEntity() != null)
                .collect(Collectors.groupingBy(
                        mf -> mf.getMedicalTypeEntity().getMedicalTypeName(),
                        Collectors.summingLong(mf -> {
                            Long amount = mf.getMedicalFeeAmount() != null ? mf.getMedicalFeeAmount() : 0L;
                            Integer quantity = mf.getQuantity() != null ? mf.getQuantity() : 1;
                            return amount * quantity;
                        })
                ));

        Map<String, Long> departmentStatistics = new HashMap<>();

        return PeriodMedicalFeeStatistics.builder()
                .startDate(startDate)
                .endDate(endDate)
                .count(count)
                .totalAmount(totalAmount)
                .typeStatistics(typeStatistics)
                .departmentStatistics(departmentStatistics)
                .build();
    }
}