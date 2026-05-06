package com.sleekydz86.emrclinical.treatment.statistics.department.service;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.service.MedicalFeeCalculator;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.treatment.statistics.department.dto.*;
import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import com.sleekydz86.emrclinical.treatment.statistics.department.repository.TreatmentDepartmentStatisticsRepository;
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
public class TreatmentDepartmentStatisticsService {

    private final TreatmentDepartmentStatisticsRepository statisticsRepository;
    private final TreatmentRepository treatmentRepository;
    private final PatientCountCalculator patientCountCalculator;
    private final MedicalFeeCalculator medicalFeeCalculator;
    private final DepartmentTreatmentFilter treatmentFilter;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "treatment:department:statistics:";
    private static final long CACHE_TTL = 86400;

    public TreatmentDepartmentStatisticsResponse getStatistics(TreatmentDepartmentStatisticsRequest request) {
        String cacheKey = buildCacheKey(request);

        @SuppressWarnings("unchecked")
        TreatmentDepartmentStatisticsResponse cached =
                (TreatmentDepartmentStatisticsResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LocalDate startDate = LocalDate.of(Integer.parseInt(request.getYear()), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(request.getYear()), 12, 31);

        Map<String, Object> internalStatistics = calculateInternalStatistics(
                startDate, endDate, request.getDepartmentName(), request.getRegionCode());

        List<TreatmentDepartmentStatisticsEntity> publicStatistics =
                statisticsRepository.findByYearAndDepartmentAndRegion(
                        request.getYear(), request.getDepartmentName(), request.getRegionCode());

        TreatmentDepartmentStatisticsResponse response = buildResponse(
                request, internalStatistics, publicStatistics);

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.SECONDS);

        return response;
    }

    public TreatmentDepartmentStatisticsByYearResponse getStatisticsByYear(String year) {
        LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);

        Map<String, Object> departmentStatistics = calculateDepartmentStatistics(startDate, endDate);

        List<TreatmentDepartmentStatisticsEntity> publicData =
                statisticsRepository.findByStatisticsYear(year);

        return TreatmentDepartmentStatisticsByYearResponse.builder()
                .year(year)
                .departmentStatistics(departmentStatistics)
                .publicStatistics(TreatmentDepartmentStatisticsByYearResponse.PublicStatistics.from(publicData))
                .build();
    }

    public TreatmentDepartmentStatisticsByRegionResponse getStatisticsByRegion(
            String year, String regionCode) {

        LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);

        Map<String, Object> statistics = calculateStatisticsByRegion(startDate, endDate, regionCode);

        List<TreatmentDepartmentStatisticsEntity> publicData =
                statisticsRepository.findByStatisticsYearAndRegionCode(year, regionCode);

        return TreatmentDepartmentStatisticsByRegionResponse.builder()
                .year(year)
                .regionCode(regionCode)
                .internalStatistics(statistics)
                .publicStatistics(TreatmentDepartmentStatisticsByRegionResponse.PublicStatistics.from(publicData))
                .build();
    }

    public TreatmentDepartmentStatisticsByDepartmentResponse getStatisticsByDepartment(
            String year, String departmentName) {

        LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);

        Map<String, Object> statistics = calculateStatisticsByDepartment(
                startDate, endDate, departmentName);

        List<TreatmentDepartmentStatisticsEntity> publicData =
                statisticsRepository.findByStatisticsYearAndDepartmentName(year, departmentName);

        return TreatmentDepartmentStatisticsByDepartmentResponse.builder()
                .year(year)
                .departmentName(departmentName)
                .internalStatistics(statistics)
                .publicStatistics(TreatmentDepartmentStatisticsByDepartmentResponse.PublicStatistics.from(publicData))
                .build();
    }

    private Map<String, Object> calculateInternalStatistics(
            LocalDate startDate, LocalDate endDate,
            String departmentName, String regionCode) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> allTreatments = treatmentRepository.findByTreatmentDateBetween(start, end);
        List<TreatmentEntity> treatments = treatmentFilter.filterTreatments(
                allTreatments, departmentName, regionCode);

        long patientCount = patientCountCalculator.calculatePatientCount(treatments);
        long treatmentCount = treatments.size();
        long medicalFee = medicalFeeCalculator.calculateMedicalFee(treatments);
        long benefitFee = medicalFeeCalculator.calculateBenefitFee(treatments);

        return Map.of(
                "patientCount", patientCount,
                "treatmentCount", treatmentCount,
                "medicalFee", medicalFee,
                "benefitFee", benefitFee,
                "averageFeePerTreatment", treatmentCount > 0 ? medicalFee / treatmentCount : 0L,
                "averageFeePerPatient", patientCount > 0 ? medicalFee / patientCount : 0L
        );
    }

    private Map<String, Object> calculateDepartmentStatistics(
            LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> treatments =
                treatmentRepository.findByTreatmentDateBetween(start, end);

        Map<String, DepartmentStatistics> statistics = treatments.stream()
                .filter(t -> t.getDepartmentEntity() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getDepartmentEntity().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long patientCount = patientCountCalculator.calculatePatientCount(list);
                                    long treatmentCount = list.size();
                                    long medicalFee = medicalFeeCalculator.calculateMedicalFee(list);
                                    long benefitFee = medicalFeeCalculator.calculateBenefitFee(list);

                                    return DepartmentStatistics.builder()
                                            .patientCount(patientCount)
                                            .treatmentCount(treatmentCount)
                                            .medicalFee(medicalFee)
                                            .benefitFee(benefitFee)
                                            .averageFeePerTreatment(treatmentCount > 0 ? medicalFee / treatmentCount : 0L)
                                            .averageFeePerPatient(patientCount > 0 ? medicalFee / patientCount : 0L)
                                            .build();
                                }
                        )
                ));

        return statistics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "patientCount", e.getValue().getPatientCount(),
                                "treatmentCount", e.getValue().getTreatmentCount(),
                                "medicalFee", e.getValue().getMedicalFee(),
                                "benefitFee", e.getValue().getBenefitFee(),
                                "averageFeePerTreatment", e.getValue().getAverageFeePerTreatment(),
                                "averageFeePerPatient", e.getValue().getAverageFeePerPatient()
                        )
                ));
    }

    private Map<String, Object> calculateStatisticsByRegion(
            LocalDate startDate, LocalDate endDate, String regionCode) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> allTreatments = treatmentRepository.findByTreatmentDateBetween(start, end);
        List<TreatmentEntity> treatments = treatmentFilter.filterTreatments(
                allTreatments, null, regionCode);

        Map<String, DepartmentStatistics> statistics = treatments.stream()
                .filter(t -> t.getDepartmentEntity() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getDepartmentEntity().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long patientCount = patientCountCalculator.calculatePatientCount(list);
                                    long treatmentCount = list.size();
                                    long medicalFee = medicalFeeCalculator.calculateMedicalFee(list);
                                    long benefitFee = medicalFeeCalculator.calculateBenefitFee(list);

                                    return DepartmentStatistics.builder()
                                            .patientCount(patientCount)
                                            .treatmentCount(treatmentCount)
                                            .medicalFee(medicalFee)
                                            .benefitFee(benefitFee)
                                            .averageFeePerTreatment(treatmentCount > 0 ? medicalFee / treatmentCount : 0L)
                                            .averageFeePerPatient(patientCount > 0 ? medicalFee / patientCount : 0L)
                                            .build();
                                }
                        )
                ));

        return statistics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "patientCount", e.getValue().getPatientCount(),
                                "treatmentCount", e.getValue().getTreatmentCount(),
                                "medicalFee", e.getValue().getMedicalFee(),
                                "benefitFee", e.getValue().getBenefitFee(),
                                "averageFeePerTreatment", e.getValue().getAverageFeePerTreatment(),
                                "averageFeePerPatient", e.getValue().getAverageFeePerPatient()
                        )
                ));
    }

    private Map<String, Object> calculateStatisticsByDepartment(
            LocalDate startDate, LocalDate endDate, String departmentName) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TreatmentEntity> allTreatments = treatmentRepository.findByTreatmentDateBetween(start, end);
        List<TreatmentEntity> treatments = treatmentFilter.filterTreatments(
                allTreatments, departmentName, null);

        Map<String, RegionStatistics> statistics = treatments.stream()
                .filter(t -> t.getPatientEntity() != null && t.getPatientEntity().getPatientAddress() != null)
                .collect(Collectors.groupingBy(
                        t -> {
                            String address = t.getPatientEntity().getPatientAddress();
                            return treatmentFilter.regionCodeExtractor.extractRegionCode(address);
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long patientCount = patientCountCalculator.calculatePatientCount(list);
                                    long treatmentCount = list.size();
                                    long medicalFee = medicalFeeCalculator.calculateMedicalFee(list);
                                    long benefitFee = medicalFeeCalculator.calculateBenefitFee(list);

                                    return RegionStatistics.builder()
                                            .patientCount(patientCount)
                                            .treatmentCount(treatmentCount)
                                            .medicalFee(medicalFee)
                                            .benefitFee(benefitFee)
                                            .averageFeePerTreatment(treatmentCount > 0 ? medicalFee / treatmentCount : 0L)
                                            .averageFeePerPatient(patientCount > 0 ? medicalFee / patientCount : 0L)
                                            .build();
                                }
                        )
                ));

        return statistics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "patientCount", e.getValue().getPatientCount(),
                                "treatmentCount", e.getValue().getTreatmentCount(),
                                "medicalFee", e.getValue().getMedicalFee(),
                                "benefitFee", e.getValue().getBenefitFee(),
                                "averageFeePerTreatment", e.getValue().getAverageFeePerTreatment(),
                                "averageFeePerPatient", e.getValue().getAverageFeePerPatient()
                        )
                ));
    }

    private TreatmentDepartmentStatisticsResponse buildResponse(
            TreatmentDepartmentStatisticsRequest request,
            Map<String, Object> internalStatistics,
            List<TreatmentDepartmentStatisticsEntity> publicStatistics) {

        List<TreatmentDepartmentStatisticsResponse.PublicStatistics> publicStats =
                publicStatistics.stream()
                        .map(TreatmentDepartmentStatisticsResponse.PublicStatistics::from)
                        .toList();

        Map<String, Object> comparison = buildComparison(internalStatistics, publicStats);

        return TreatmentDepartmentStatisticsResponse.builder()
                .year(request.getYear())
                .departmentName(request.getDepartmentName())
                .regionCode(request.getRegionCode())
                .internalStatistics(internalStatistics)
                .publicStatistics(publicStats)
                .comparison(comparison)
                .build();
    }

    private Map<String, Object> buildComparison(
            Map<String, Object> internalStatistics,
            List<TreatmentDepartmentStatisticsResponse.PublicStatistics> publicStatistics) {

        if (publicStatistics.isEmpty()) {
            return Map.of("available", false);
        }

        TreatmentDepartmentStatisticsResponse.PublicStatistics publicData = publicStatistics.get(0);
        long internalPatientCount = ((Number) internalStatistics.get("patientCount")).longValue();
        long internalTreatmentCount = ((Number) internalStatistics.get("treatmentCount")).longValue();
        long internalMedicalFee = ((Number) internalStatistics.get("medicalFee")).longValue();
        long internalBenefitFee = ((Number) internalStatistics.get("benefitFee")).longValue();

        return Map.of(
                "available", true,
                "patientCount", Map.of(
                        "internal", internalPatientCount,
                        "public", publicData.getPatientCount(),
                        "difference", internalPatientCount - publicData.getPatientCount(),
                        "percentage", publicData.getPatientCount() > 0
                                ? (double) (internalPatientCount - publicData.getPatientCount()) / publicData.getPatientCount() * 100
                                : 0.0
                ),
                "treatmentCount", Map.of(
                        "internal", internalTreatmentCount,
                        "public", publicData.getTreatmentCount(),
                        "difference", internalTreatmentCount - publicData.getTreatmentCount(),
                        "percentage", publicData.getTreatmentCount() > 0
                                ? (double) (internalTreatmentCount - publicData.getTreatmentCount()) / publicData.getTreatmentCount() * 100
                                : 0.0
                ),
                "medicalFee", Map.of(
                        "internal", internalMedicalFee,
                        "public", publicData.getMedicalFee(),
                        "difference", internalMedicalFee - publicData.getMedicalFee(),
                        "percentage", publicData.getMedicalFee() > 0
                                ? (double) (internalMedicalFee - publicData.getMedicalFee()) / publicData.getMedicalFee() * 100
                                : 0.0
                ),
                "benefitFee", Map.of(
                        "internal", internalBenefitFee,
                        "public", publicData.getBenefitFee(),
                        "difference", internalBenefitFee - publicData.getBenefitFee(),
                        "percentage", publicData.getBenefitFee() > 0
                                ? (double) (internalBenefitFee - publicData.getBenefitFee()) / publicData.getBenefitFee() * 100
                                : 0.0
                )
        );
    }

    private String buildCacheKey(TreatmentDepartmentStatisticsRequest request) {
        return CACHE_PREFIX +
                request.getYear() + ":" +
                (request.getDepartmentName() != null ? request.getDepartmentName() : "") + ":" +
                (request.getRegionCode() != null ? request.getRegionCode() : "");
    }

    @lombok.Getter
    @lombok.Builder
    private static class DepartmentStatistics {
        private Long patientCount;
        private Long treatmentCount;
        private Long medicalFee;
        private Long benefitFee;
        private Long averageFeePerTreatment;
        private Long averageFeePerPatient;
    }

    @lombok.Getter
    @lombok.Builder
    private static class RegionStatistics {
        private Long patientCount;
        private Long treatmentCount;
        private Long medicalFee;
        private Long benefitFee;
        private Long averageFeePerTreatment;
        private Long averageFeePerPatient;
    }
}

