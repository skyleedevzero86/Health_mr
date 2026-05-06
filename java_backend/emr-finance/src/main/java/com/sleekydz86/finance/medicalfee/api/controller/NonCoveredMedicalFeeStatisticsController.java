package com.sleekydz86.finance.medicalfee.api.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.finance.medicalfee.service.NonCoveredMedicalFeeStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/non-covered-medical-fee/statistics")
@RequiredArgsConstructor
public class NonCoveredMedicalFeeStatisticsController {

    private final NonCoveredMedicalFeeStatisticsService statisticsService;

    @GetMapping("/by-institution-type")
    @AuthRole({"STAFF", "ADMIN"})
    public Mono<ResponseEntity<Map<String, Object>>> getStatisticsByInstitutionType(
            @RequestParam String npayCd
    ) {
        return statisticsService.getStatisticsByInstitutionType(npayCd)
                .map(data -> ResponseEntity.ok(Map.of(
                        "message", "종별 통계 조회 성공",
                        "data", data
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "종별 통계 조회 실패"
                )));
    }

    @GetMapping("/by-region")
    @AuthRole({"STAFF", "ADMIN"})
    public Mono<ResponseEntity<Map<String, Object>>> getStatisticsByRegion(
            @RequestParam String npayCd
    ) {
        return statisticsService.getStatisticsByRegion(npayCd)
                .map(data -> ResponseEntity.ok(Map.of(
                        "message", "지역별 통계 조회 성공",
                        "data", data
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "지역별 통계 조회 실패"
                )));
    }

    @GetMapping("/price-comparison")
    @AuthRole({"STAFF", "ADMIN"})
    public Mono<ResponseEntity<Map<String, Object>>> getPriceComparison(
            @RequestParam String npayCd,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String institutionType
    ) {
        return statisticsService.getPriceComparison(npayCd, region, institutionType)
                .map(data -> ResponseEntity.ok(Map.of(
                        "message", "가격 비교 분석 성공",
                        "data", data
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "가격 비교 분석 실패"
                )));
    }
}

