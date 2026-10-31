package com.sleekydz86.emrclinical.treatment.statistics;

import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
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
public class TreatmentStatisticsService {

    private final TreatmentRepository treatmentRepository;
    private final com.sleekydz86.emrclinical.treatment.inpatient.statistics.service.InpatientStatisticsService inpatientStatisticsService;
    private final com.sleekydz86.emrclinical.treatment.statistics.department.service.TreatmentDepartmentStatisticsService departmentStatisticsService;

    public TreatmentStatisticsResponse getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        Long totalCount = treatmentRepository.countByDateRange(start, end);
        Long completedCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.COMPLETED)
                .count();
        Long pendingCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.PENDING)
                .count();
        Long inProgressCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.IN_PROGRESS)
                .count();
        Long cancelledCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.CANCELLED)
                .count();

        Map<TreatmentType, Long> typeCount = new HashMap<>();
        for (TreatmentType type : TreatmentType.values()) {
            long count = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                    .filter(t -> t.getTreatmentType() == type)
                    .count();
            typeCount.put(type, count);
        }

        return TreatmentStatisticsResponse.builder()
                .date(date)
                .totalCount(totalCount)
                .completedCount(completedCount)
                .pendingCount(pendingCount)
                .inProgressCount(inProgressCount)
                .cancelledCount(cancelledCount)
                .typeCount(typeCount)
                .build();
    }

    public TreatmentStatisticsResponse getPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long totalCount = treatmentRepository.countByDateRange(start, end);
        Long completedCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.COMPLETED)
                .count();
        Long pendingCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.PENDING)
                .count();
        Long inProgressCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.IN_PROGRESS)
                .count();
        Long cancelledCount = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.CANCELLED)
                .count();

        Map<TreatmentType, Long> typeCount = new HashMap<>();
        for (TreatmentType type : TreatmentType.values()) {
            long count = treatmentRepository.findByTreatmentDateBetween(start, end).stream()
                    .filter(t -> t.getTreatmentType() == type)
                    .count();
            typeCount.put(type, count);
        }

        return TreatmentStatisticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCount(totalCount)
                .completedCount(completedCount)
                .pendingCount(pendingCount)
                .inProgressCount(inProgressCount)
                .cancelledCount(cancelledCount)
                .typeCount(typeCount)
                .build();
    }

    public TreatmentStatisticsResponse getDoctorStatistics(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long totalCount = treatmentRepository.countByDoctorAndDateRange(doctorId, start, end);
        Long completedCount = treatmentRepository.findByTreatmentDoc_Id(doctorId).stream()
                .filter(t -> t.getTreatmentDate().isAfter(start) && t.getTreatmentDate().isBefore(end))
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.COMPLETED)
                .count();
        Long pendingCount = treatmentRepository.findByTreatmentDoc_Id(doctorId).stream()
                .filter(t -> t.getTreatmentDate().isAfter(start) && t.getTreatmentDate().isBefore(end))
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.PENDING)
                .count();
        Long inProgressCount = treatmentRepository.findByTreatmentDoc_Id(doctorId).stream()
                .filter(t -> t.getTreatmentDate().isAfter(start) && t.getTreatmentDate().isBefore(end))
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.IN_PROGRESS)
                .count();
        Long cancelledCount = treatmentRepository.findByTreatmentDoc_Id(doctorId).stream()
                .filter(t -> t.getTreatmentDate().isAfter(start) && t.getTreatmentDate().isBefore(end))
                .filter(t -> t.getTreatmentStatus() == TreatmentStatus.CANCELLED)
                .count();

        Map<TreatmentType, Long> typeCount = new HashMap<>();
        for (TreatmentType type : TreatmentType.values()) {
            long count = treatmentRepository.findByTreatmentDoc_Id(doctorId).stream()
                    .filter(t -> t.getTreatmentDate().isAfter(start) && t.getTreatmentDate().isBefore(end))
                    .filter(t -> t.getTreatmentType() == type)
                    .count();
            typeCount.put(type, count);
        }

        return TreatmentStatisticsResponse.builder()
                .doctorId(doctorId)
                .startDate(startDate)
                .endDate(endDate)
                .totalCount(totalCount)
                .completedCount(completedCount)
                .pendingCount(pendingCount)
                .inProgressCount(inProgressCount)
                .cancelledCount(cancelledCount)
                .typeCount(typeCount)
                .build();
    }

    public Map<String, Object> getInpatientStatisticsByYear(String year) {
        com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.InpatientStatisticsByYearResponse response =
                inpatientStatisticsService.getStatisticsByYear(year);
        return Map.of(
                "year", response.getYear(),
                "inpatientStatistics", response.getInpatientStatistics(),
                "outpatientStatistics", response.getOutpatientStatistics(),
                "prescriptionStatistics", response.getPrescriptionStatistics(),
                "totalStatistics", response.getTotalStatistics()
        );
    }

    public Map<String, Object> getInpatientStatisticsByRegion(String year, String regionCode) {
        com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.InpatientStatisticsByRegionResponse response =
                inpatientStatisticsService.getStatisticsByRegion(year, regionCode);
        return Map.of(
                "year", response.getYear(),
                "regionCode", response.getRegionCode(),
                "internalStatistics", response.getInternalStatistics(),
                "publicStatistics", response.getPublicStatistics()
        );
    }

    public Map<String, Object> getDepartmentStatisticsByYear(String year) {
        com.sleekydz86.emrclinical.treatment.statistics.department.dto.TreatmentDepartmentStatisticsByYearResponse response =
                departmentStatisticsService.getStatisticsByYear(year);
        return Map.of(
                "year", response.getYear(),
                "departmentStatistics", response.getDepartmentStatistics(),
                "publicStatistics", response.getPublicStatistics()
        );
    }

    public Map<String, Object> getDepartmentStatisticsByRegion(String year, String regionCode) {
        com.sleekydz86.emrclinical.treatment.statistics.department.dto.TreatmentDepartmentStatisticsByRegionResponse response =
                departmentStatisticsService.getStatisticsByRegion(year, regionCode);
        return Map.of(
                "year", response.getYear(),
                "regionCode", response.getRegionCode(),
                "internalStatistics", response.getInternalStatistics(),
                "publicStatistics", response.getPublicStatistics()
        );
    }

    public Map<String, Object> getDepartmentStatisticsByDepartment(String year, String departmentName) {
        com.sleekydz86.emrclinical.treatment.statistics.department.dto.TreatmentDepartmentStatisticsByDepartmentResponse response =
                departmentStatisticsService.getStatisticsByDepartment(year, departmentName);
        return Map.of(
                "year", response.getYear(),
                "departmentName", response.getDepartmentName(),
                "internalStatistics", response.getInternalStatistics(),
                "publicStatistics", response.getPublicStatistics()
        );
    }
}