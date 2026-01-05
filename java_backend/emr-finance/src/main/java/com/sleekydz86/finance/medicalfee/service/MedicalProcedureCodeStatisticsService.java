package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.finance.medicalfee.dto.*;
import com.sleekydz86.finance.medicalfee.entity.MedicalProcedureCodeStatisticsEntity;
import com.sleekydz86.finance.medicalfee.repository.MedicalProcedureCodeStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicalProcedureCodeStatisticsService {

    private final MedicalProcedureCodeStatisticsRepository statisticsRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "procedure:code:statistics:";
    private static final long CACHE_TTL = 86400;

    public ProcedureCodeStatisticsResponse getStatisticsByCode(ProcedureCodeStatisticsRequest request) {
        String cacheKey = buildCacheKey(request);
        
        @SuppressWarnings("unchecked")
        ProcedureCodeStatisticsResponse cached = 
                (ProcedureCodeStatisticsResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<MedicalProcedureCodeStatisticsEntity> statistics = 
            statisticsRepository.findByProcedureCodeAndYearRange(
                request.getProcedureCode(),
                request.getStartYear(),
                request.getEndYear(),
                request.getInstitutionType()
            );

        ProcedureCodeStatisticsResponse response = buildResponse(statistics, request);
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.SECONDS);
        
        return response;
    }

    public ProcedureCodeStatisticsByYearResponse getStatisticsByYear(String procedureCode, String year) {
        List<MedicalProcedureCodeStatisticsEntity> statistics = 
            statisticsRepository.findByProcedureCodeAndTreatmentYear(procedureCode, year);

        Map<String, ProcedureCodeStatisticsByYearResponse.InstitutionStatistics> institutionStats = 
            statistics.stream()
                .collect(Collectors.toMap(
                    MedicalProcedureCodeStatisticsEntity::getInstitutionType,
                    stat -> {
                        double avg = stat.getPatientCount() > 0 
                            ? (double) stat.getTreatmentCount() / stat.getPatientCount()
                            : 0.0;
                        return ProcedureCodeStatisticsByYearResponse.InstitutionStatistics.builder()
                            .patientCount(stat.getPatientCount())
                            .treatmentCount(stat.getTreatmentCount())
                            .averageTreatmentsPerPatient(avg)
                            .build();
                    }
                ));

        Long totalPatients = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
            .sum();

        Long totalTreatments = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
            .sum();

        return ProcedureCodeStatisticsByYearResponse.builder()
            .procedureCode(procedureCode)
            .year(year)
            .totalPatients(totalPatients)
            .totalTreatments(totalTreatments)
            .institutionStatistics(institutionStats)
            .build();
    }

    public ProcedureCodeStatisticsByInstitutionResponse getStatisticsByInstitution(
            String procedureCode, String institutionType) {
        
        List<MedicalProcedureCodeStatisticsEntity> statistics = 
            statisticsRepository.findByProcedureCodeAndInstitutionType(procedureCode, institutionType);

        Map<String, ProcedureCodeStatisticsByInstitutionResponse.YearStatistics> yearStats = 
            statistics.stream()
                .collect(Collectors.toMap(
                    MedicalProcedureCodeStatisticsEntity::getTreatmentYear,
                    stat -> {
                        double avg = stat.getPatientCount() > 0 
                            ? (double) stat.getTreatmentCount() / stat.getPatientCount()
                            : 0.0;
                        return ProcedureCodeStatisticsByInstitutionResponse.YearStatistics.builder()
                            .patientCount(stat.getPatientCount())
                            .treatmentCount(stat.getTreatmentCount())
                            .averageTreatmentsPerPatient(avg)
                            .build();
                    }
                ));

        Long totalPatients = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
            .sum();

        Long totalTreatments = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
            .sum();

        return ProcedureCodeStatisticsByInstitutionResponse.builder()
            .procedureCode(procedureCode)
            .institutionType(institutionType)
            .yearStatistics(yearStats)
            .totalPatients(totalPatients)
            .totalTreatments(totalTreatments)
            .build();
    }

    public Map<String, Object> getComparisonStatistics(
            String procedureCode1, String procedureCode2, String year) {
        
        List<MedicalProcedureCodeStatisticsEntity> stats1 = 
            statisticsRepository.findByProcedureCodeAndTreatmentYear(procedureCode1, year);
        List<MedicalProcedureCodeStatisticsEntity> stats2 = 
            statisticsRepository.findByProcedureCodeAndTreatmentYear(procedureCode2, year);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("procedureCode1", buildStatisticsMap(stats1));
        comparison.put("procedureCode2", buildStatisticsMap(stats2));
        comparison.put("comparison", buildComparison(stats1, stats2));

        return comparison;
    }

    private ProcedureCodeStatisticsResponse buildResponse(
            List<MedicalProcedureCodeStatisticsEntity> statistics,
            ProcedureCodeStatisticsRequest request) {

        Long totalPatients = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
            .sum();

        Long totalTreatments = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
            .sum();

        Map<String, List<MedicalProcedureCodeStatisticsEntity>> byYear = statistics.stream()
            .collect(Collectors.groupingBy(MedicalProcedureCodeStatisticsEntity::getTreatmentYear));

        List<ProcedureCodeStatisticsResponse.YearStatistics> yearStatistics = byYear.entrySet().stream()
            .map(entry -> {
                String year = entry.getKey();
                List<MedicalProcedureCodeStatisticsEntity> yearStats = entry.getValue();
                
                Map<String, ProcedureCodeStatisticsResponse.InstitutionStatistics> byInstitution = 
                    yearStats.stream()
                        .collect(Collectors.toMap(
                            MedicalProcedureCodeStatisticsEntity::getInstitutionType,
                            stat -> {
                                double avg = stat.getPatientCount() > 0 
                                    ? (double) stat.getTreatmentCount() / stat.getPatientCount()
                                    : 0.0;
                                return ProcedureCodeStatisticsResponse.InstitutionStatistics.builder()
                                    .institutionType(stat.getInstitutionType())
                                    .patientCount(stat.getPatientCount())
                                    .treatmentCount(stat.getTreatmentCount())
                                    .averageTreatmentsPerPatient(avg)
                                    .build();
                            }
                        ));

                Long yearPatients = yearStats.stream()
                    .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
                    .sum();

                Long yearTreatments = yearStats.stream()
                    .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
                    .sum();

                return ProcedureCodeStatisticsResponse.YearStatistics.builder()
                    .year(year)
                    .patientCount(yearPatients)
                    .treatmentCount(yearTreatments)
                    .byInstitution(byInstitution)
                    .build();
            })
            .sorted(Comparator.comparing(ProcedureCodeStatisticsResponse.YearStatistics::getYear))
            .collect(Collectors.toList());

        Map<String, List<MedicalProcedureCodeStatisticsEntity>> byInstitution = statistics.stream()
            .collect(Collectors.groupingBy(MedicalProcedureCodeStatisticsEntity::getInstitutionType));

        List<ProcedureCodeStatisticsResponse.InstitutionStatistics> institutionStatistics = 
            byInstitution.entrySet().stream()
                .map(entry -> {
                    List<MedicalProcedureCodeStatisticsEntity> instStats = entry.getValue();
                    Long instPatients = instStats.stream()
                        .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
                        .sum();
                    Long instTreatments = instStats.stream()
                        .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
                        .sum();
                    double avg = instPatients > 0 
                        ? (double) instTreatments / instPatients
                        : 0.0;
                    return ProcedureCodeStatisticsResponse.InstitutionStatistics.builder()
                        .institutionType(entry.getKey())
                        .patientCount(instPatients)
                        .treatmentCount(instTreatments)
                        .averageTreatmentsPerPatient(avg)
                        .build();
                })
                .collect(Collectors.toList());

        return ProcedureCodeStatisticsResponse.builder()
            .procedureCode(request.getProcedureCode())
            .startYear(request.getStartYear())
            .endYear(request.getEndYear())
            .totalPatients(totalPatients)
            .totalTreatments(totalTreatments)
            .yearStatistics(yearStatistics)
            .institutionStatistics(institutionStatistics)
            .build();
    }

    private Map<String, Object> buildStatisticsMap(List<MedicalProcedureCodeStatisticsEntity> statistics) {
        Long totalPatients = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
            .sum();
        Long totalTreatments = statistics.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
            .sum();

        Map<String, Object> institutionStats = statistics.stream()
            .collect(Collectors.toMap(
                MedicalProcedureCodeStatisticsEntity::getInstitutionType,
                stat -> Map.of(
                    "patientCount", stat.getPatientCount(),
                    "treatmentCount", stat.getTreatmentCount()
                )
            ));

        return Map.of(
            "totalPatients", totalPatients,
            "totalTreatments", totalTreatments,
            "institutionStatistics", institutionStats
        );
    }

    private Map<String, Object> buildComparison(
            List<MedicalProcedureCodeStatisticsEntity> stats1,
            List<MedicalProcedureCodeStatisticsEntity> stats2) {
        
        Long patients1 = stats1.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
            .sum();
        Long patients2 = stats2.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getPatientCount)
            .sum();

        Long treatments1 = stats1.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
            .sum();
        Long treatments2 = stats2.stream()
            .mapToLong(MedicalProcedureCodeStatisticsEntity::getTreatmentCount)
            .sum();

        double patientDiff = patients1 > 0 
            ? ((double) (patients2 - patients1) / patients1) * 100
            : 0.0;
        double treatmentDiff = treatments1 > 0 
            ? ((double) (treatments2 - treatments1) / treatments1) * 100
            : 0.0;

        return Map.of(
            "patientDifference", patientDiff,
            "treatmentDifference", treatmentDiff,
            "patientDifferenceCount", patients2 - patients1,
            "treatmentDifferenceCount", treatments2 - treatments1
        );
    }

    private String buildCacheKey(ProcedureCodeStatisticsRequest request) {
        return CACHE_PREFIX + 
                request.getProcedureCode() + ":" +
                (request.getStartYear() != null ? request.getStartYear() : "") + ":" +
                (request.getEndYear() != null ? request.getEndYear() : "") + ":" +
                (request.getInstitutionType() != null ? request.getInstitutionType() : "");
    }
}

