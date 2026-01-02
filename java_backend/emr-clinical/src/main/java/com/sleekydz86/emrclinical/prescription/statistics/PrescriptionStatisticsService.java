package com.sleekydz86.emrclinical.prescription.statistics;

import com.sleekydz86.emrclinical.prescription.repository.PrescriptionRepository;
import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionStatisticsService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionStatisticsResponse getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        Long totalCount = prescriptionRepository.countByDateRange(start, end);
        Long prescribedCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.PRESCRIBED)
                .count();
        Long dispensedCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.DISPENSED)
                .count();
        Long pendingCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.PENDING)
                .count();
        Long cancelledCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.CANCELLED)
                .count();

        Map<PrescriptionType, Long> typeCount = new HashMap<>();
        for (PrescriptionType type : PrescriptionType.values()) {
            long count = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                    .filter(p -> p.getPrescriptionType() == type)
                    .count();
            typeCount.put(type, count);
        }

        return PrescriptionStatisticsResponse.builder()
                .date(date)
                .totalCount(totalCount)
                .prescribedCount(prescribedCount)
                .dispensedCount(dispensedCount)
                .pendingCount(pendingCount)
                .cancelledCount(cancelledCount)
                .typeCount(typeCount)
                .build();
    }

    public PrescriptionStatisticsResponse getPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long totalCount = prescriptionRepository.countByDateRange(start, end);
        Long prescribedCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.PRESCRIBED)
                .count();
        Long dispensedCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.DISPENSED)
                .count();
        Long pendingCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.PENDING)
                .count();
        Long cancelledCount = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.CANCELLED)
                .count();

        Map<PrescriptionType, Long> typeCount = new HashMap<>();
        for (PrescriptionType type : PrescriptionType.values()) {
            long count = prescriptionRepository.findByPrescriptionDateBetween(start, end).stream()
                    .filter(p -> p.getPrescriptionType() == type)
                    .count();
            typeCount.put(type, count);
        }

        return PrescriptionStatisticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCount(totalCount)
                .prescribedCount(prescribedCount)
                .dispensedCount(dispensedCount)
                .pendingCount(pendingCount)
                .cancelledCount(cancelledCount)
                .typeCount(typeCount)
                .build();
    }

    public PrescriptionStatisticsResponse getDoctorStatistics(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long totalCount = prescriptionRepository.findByPrescriptionDoc_Id(doctorId).stream()
                .filter(p -> p.getPrescriptionDate().isAfter(start) && p.getPrescriptionDate().isBefore(end))
                .count();
        Long prescribedCount = prescriptionRepository.findByPrescriptionDoc_Id(doctorId).stream()
                .filter(p -> p.getPrescriptionDate().isAfter(start) && p.getPrescriptionDate().isBefore(end))
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.PRESCRIBED)
                .count();
        Long dispensedCount = prescriptionRepository.findByPrescriptionDoc_Id(doctorId).stream()
                .filter(p -> p.getPrescriptionDate().isAfter(start) && p.getPrescriptionDate().isBefore(end))
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.DISPENSED)
                .count();
        Long pendingCount = prescriptionRepository.findByPrescriptionDoc_Id(doctorId).stream()
                .filter(p -> p.getPrescriptionDate().isAfter(start) && p.getPrescriptionDate().isBefore(end))
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.PENDING)
                .count();
        Long cancelledCount = prescriptionRepository.findByPrescriptionDoc_Id(doctorId).stream()
                .filter(p -> p.getPrescriptionDate().isAfter(start) && p.getPrescriptionDate().isBefore(end))
                .filter(p -> p.getPrescriptionStatus() == PrescriptionStatus.CANCELLED)
                .count();

        Map<PrescriptionType, Long> typeCount = new HashMap<>();
        for (PrescriptionType type : PrescriptionType.values()) {
            long count = prescriptionRepository.findByPrescriptionDoc_Id(doctorId).stream()
                    .filter(p -> p.getPrescriptionDate().isAfter(start) && p.getPrescriptionDate().isBefore(end))
                    .filter(p -> p.getPrescriptionType() == type)
                    .count();
            typeCount.put(type, count);
        }

        return PrescriptionStatisticsResponse.builder()
                .doctorId(doctorId)
                .startDate(startDate)
                .endDate(endDate)
                .totalCount(totalCount)
                .prescribedCount(prescribedCount)
                .dispensedCount(dispensedCount)
                .pendingCount(pendingCount)
                .cancelledCount(cancelledCount)
                .typeCount(typeCount)
                .build();
    }
}

