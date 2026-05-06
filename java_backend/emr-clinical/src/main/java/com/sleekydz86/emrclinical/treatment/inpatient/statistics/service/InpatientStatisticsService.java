package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.prescription.repository.PrescriptionRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.*;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.repository.InpatientStatisticsRepository;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InpatientStatisticsService {

    private final InpatientStatisticsRepository statisticsRepository;
    private final TreatmentRepository treatmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final VisitDaysCalculator visitDaysCalculator;
    private final BenefitDaysCalculator benefitDaysCalculator;
    private final MedicalFeeCalculator medicalFeeCalculator;
    private final TreatmentFilter treatmentFilter;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "inpatient:statistics:";
    private static final long CACHE_TTL = 86400;

    public InpatientStatisticsResponse getStatistics(InpatientStatisticsRequest request) {
        String cacheKey = buildCacheKey(request);
        
        @SuppressWarnings("unchecked")
        InpatientStatisticsResponse cached = 
                (InpatientStatisticsResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LocalDate startDate = LocalDate.of(Integer.parseInt(request.getYear()), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(request.getYear()), 12, 31);

        Map<String, Object> internalStatistics = calculateInternalStatistics(
                startDate, endDate, request.getInstitutionType(), request.getRegionCode());

        List<InpatientStatisticsEntity> publicStatistics = statisticsRepository.findByYearAndTypeAndRegion(
                request.getYear(), request.getInstitutionType(), request.getRegionCode());

        InpatientStatisticsResponse response = buildResponse(
                request, internalStatistics, publicStatistics);

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.SECONDS);
        
        return response;
    }

    public InpatientStatisticsByYearResponse getStatisticsByYear(String year) {
        LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);

        Map<String, Object> inpatientStats = calculateInpatientStatistics(startDate, endDate);
        Map<String, Object> outpatientStats = calculateOutpatientStatistics(startDate, endDate);
        Map<String, Object> prescriptionStats = calculatePrescriptionStatistics(startDate, endDate);

        return InpatientStatisticsByYearResponse.builder()
                .year(year)
                .inpatientStatistics(inpatientStats)
                .outpatientStatistics(outpatientStats)
                .prescriptionStatistics(prescriptionStats)
                .totalStatistics(calculateTotalStatistics(inpatientStats, outpatientStats, prescriptionStats))
                .build();
    }

    public InpatientStatisticsByRegionResponse getStatisticsByRegion(String year, String regionCode) {
        LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);

        Map<String, Object> statistics = calculateStatisticsByRegion(startDate, endDate, regionCode);

        List<InpatientStatisticsEntity> publicData = statisticsRepository
                .findByStatisticsYearAndRegionCode(year, regionCode);

        return InpatientStatisticsByRegionResponse.builder()
                .year(year)
                .regionCode(regionCode)
                .internalStatistics(statistics)
                .publicStatistics(InpatientStatisticsByRegionResponse.PublicStatistics.from(publicData))
                .build();
    }

    private Map<String, Object> calculateInternalStatistics(
            LocalDate startDate, LocalDate endDate,
            String institutionType, String regionCode) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> allTreatments = treatmentRepository.findByTreatmentDateBetween(start, end);
        List<TreatmentEntity> treatments = treatmentFilter.filterTreatments(
                allTreatments, institutionType, regionCode);

        long visitDays = visitDaysCalculator.calculateVisitDays(treatments);
        long benefitDays = benefitDaysCalculator.calculateBenefitDays(treatments, startDate, endDate);
        long medicalFee = medicalFeeCalculator.calculateMedicalFee(treatments);
        long benefitFee = medicalFeeCalculator.calculateBenefitFee(treatments);

        return Map.of(
                "visitDays", visitDays,
                "benefitDays", benefitDays,
                "medicalFee", medicalFee,
                "benefitFee", benefitFee,
                "treatmentCount", treatments.size()
        );
    }

    private Map<String, Object> calculateInpatientStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(start, end)
                .stream()
                .filter(t -> t.getTreatmentType() == TreatmentType.INPATIENT)
                .collect(Collectors.toList());

        long visitDays = visitDaysCalculator.calculateVisitDays(treatments);
        long benefitDays = benefitDaysCalculator.calculateBenefitDays(treatments, startDate, endDate);
        long medicalFee = medicalFeeCalculator.calculateMedicalFee(treatments);
        long benefitFee = medicalFeeCalculator.calculateBenefitFee(treatments);

        return Map.of(
                "visitDays", visitDays,
                "benefitDays", benefitDays,
                "medicalFee", medicalFee,
                "benefitFee", benefitFee,
                "treatmentCount", treatments.size()
        );
    }

    private Map<String, Object> calculateOutpatientStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(start, end)
                .stream()
                .filter(t -> t.getTreatmentType() == TreatmentType.OUTPATIENT)
                .collect(Collectors.toList());

        long visitDays = visitDaysCalculator.calculateVisitDays(treatments);
        long benefitDays = benefitDaysCalculator.calculateBenefitDays(treatments, startDate, endDate);
        long medicalFee = medicalFeeCalculator.calculateMedicalFee(treatments);
        long benefitFee = medicalFeeCalculator.calculateBenefitFee(treatments);

        return Map.of(
                "visitDays", visitDays,
                "benefitDays", benefitDays,
                "medicalFee", medicalFee,
                "benefitFee", benefitFee,
                "treatmentCount", treatments.size()
        );
    }

    private Map<String, Object> calculatePrescriptionStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity> prescriptions =
                prescriptionRepository.findByPrescriptionDateBetween(start, end);

        long prescriptionCount = prescriptions.size();
        long totalDays = prescriptions.stream()
                .filter(p -> p.getPrescriptionItems() != null)
                .flatMap(p -> p.getPrescriptionItems().stream())
                .mapToInt(item -> item.getDays() != null ? item.getDays() : 0)
                .sum();

        return Map.of(
                "prescriptionCount", prescriptionCount,
                "totalDays", totalDays
        );
    }

    private Map<String, Object> calculateTotalStatistics(
            Map<String, Object> inpatientStats,
            Map<String, Object> outpatientStats,
            Map<String, Object> prescriptionStats) {

        long totalVisitDays = ((Number) inpatientStats.get("visitDays")).longValue() +
                ((Number) outpatientStats.get("visitDays")).longValue();
        long totalBenefitDays = ((Number) inpatientStats.get("benefitDays")).longValue() +
                ((Number) outpatientStats.get("benefitDays")).longValue();
        long totalMedicalFee = ((Number) inpatientStats.get("medicalFee")).longValue() +
                ((Number) outpatientStats.get("medicalFee")).longValue();
        long totalBenefitFee = ((Number) inpatientStats.get("benefitFee")).longValue() +
                ((Number) outpatientStats.get("benefitFee")).longValue();

        return Map.of(
                "totalVisitDays", totalVisitDays,
                "totalBenefitDays", totalBenefitDays,
                "totalMedicalFee", totalMedicalFee,
                "totalBenefitFee", totalBenefitFee
        );
    }

    private Map<String, Object> calculateStatisticsByRegion(
            LocalDate startDate, LocalDate endDate, String regionCode) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> allTreatments = treatmentRepository.findByTreatmentDateBetween(start, end);
        List<TreatmentEntity> treatments = treatmentFilter.filterTreatments(
                allTreatments, null, regionCode);

        long visitDays = visitDaysCalculator.calculateVisitDays(treatments);
        long benefitDays = benefitDaysCalculator.calculateBenefitDays(treatments, startDate, endDate);
        long medicalFee = medicalFeeCalculator.calculateMedicalFee(treatments);
        long benefitFee = medicalFeeCalculator.calculateBenefitFee(treatments);

        return Map.of(
                "visitDays", visitDays,
                "benefitDays", benefitDays,
                "medicalFee", medicalFee,
                "benefitFee", benefitFee,
                "treatmentCount", treatments.size()
        );
    }

    private InpatientStatisticsResponse buildResponse(
            InpatientStatisticsRequest request,
            Map<String, Object> internalStatistics,
            List<InpatientStatisticsEntity> publicStatistics) {

        List<InpatientStatisticsResponse.PublicStatistics> publicStats = publicStatistics.stream()
                .map(InpatientStatisticsResponse.PublicStatistics::from)
                .collect(Collectors.toList());

        Map<String, Object> comparison = buildComparison(internalStatistics, publicStats);

        return InpatientStatisticsResponse.builder()
                .year(request.getYear())
                .institutionType(request.getInstitutionType())
                .regionCode(request.getRegionCode())
                .internalStatistics(internalStatistics)
                .publicStatistics(publicStats)
                .comparison(comparison)
                .build();
    }

    private Map<String, Object> buildComparison(
            Map<String, Object> internalStatistics,
            List<InpatientStatisticsResponse.PublicStatistics> publicStatistics) {

        if (publicStatistics.isEmpty()) {
            return Map.of("available", false);
        }

        long internalVisitDays = ((Number) internalStatistics.get("visitDays")).longValue();
        long internalBenefitDays = ((Number) internalStatistics.get("benefitDays")).longValue();
        long internalMedicalFee = ((Number) internalStatistics.get("medicalFee")).longValue();
        long internalBenefitFee = ((Number) internalStatistics.get("benefitFee")).longValue();

        long publicVisitDays = publicStatistics.stream()
                .mapToLong(InpatientStatisticsResponse.PublicStatistics::getVisitDays)
                .sum();
        long publicBenefitDays = publicStatistics.stream()
                .mapToLong(InpatientStatisticsResponse.PublicStatistics::getBenefitDays)
                .sum();
        long publicMedicalFee = publicStatistics.stream()
                .mapToLong(InpatientStatisticsResponse.PublicStatistics::getMedicalFee)
                .sum();
        long publicBenefitFee = publicStatistics.stream()
                .mapToLong(InpatientStatisticsResponse.PublicStatistics::getBenefitFee)
                .sum();

        double visitDaysDiff = publicVisitDays > 0
                ? ((double) (internalVisitDays - publicVisitDays) / publicVisitDays) * 100
                : 0.0;
        double benefitDaysDiff = publicBenefitDays > 0
                ? ((double) (internalBenefitDays - publicBenefitDays) / publicBenefitDays) * 100
                : 0.0;
        double medicalFeeDiff = publicMedicalFee > 0
                ? ((double) (internalMedicalFee - publicMedicalFee) / publicMedicalFee) * 100
                : 0.0;
        double benefitFeeDiff = publicBenefitFee > 0
                ? ((double) (internalBenefitFee - publicBenefitFee) / publicBenefitFee) * 100
                : 0.0;

        return Map.of(
                "available", true,
                "visitDaysDifference", visitDaysDiff,
                "benefitDaysDifference", benefitDaysDiff,
                "medicalFeeDifference", medicalFeeDiff,
                "benefitFeeDifference", benefitFeeDiff,
                "visitDaysDifferenceCount", internalVisitDays - publicVisitDays,
                "benefitDaysDifferenceCount", internalBenefitDays - publicBenefitDays,
                "medicalFeeDifferenceCount", internalMedicalFee - publicMedicalFee,
                "benefitFeeDifferenceCount", internalBenefitFee - publicBenefitFee
        );
    }

    private String buildCacheKey(InpatientStatisticsRequest request) {
        return CACHE_PREFIX +
                request.getYear() + ":" +
                (request.getInstitutionType() != null ? request.getInstitutionType() : "") + ":" +
                (request.getRegionCode() != null ? request.getRegionCode() : "");
    }
}

