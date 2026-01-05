package com.sleekydz86.finance.medicalfee.api.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.finance.medicalfee.api.NonCoveredMedicalFeeApiClient;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemCodeList2Response;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemClcdListResponse;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemSidoCdListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/non-covered-medical-fee")
@RequiredArgsConstructor
public class NonCoveredMedicalFeeApiController {

    private final NonCoveredMedicalFeeApiClient apiClient;

    @GetMapping("/codes")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public Mono<ResponseEntity<Map<String, Object>>> getNonPaymentItemCodeList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return apiClient.getNonPaymentItemCodeList2(pageNo, numOfRows)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "비급여 항목 코드 조회 성공",
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "비급여 항목 코드 조회 실패"
                )));
    }

    @GetMapping("/statistics/by-institution-type")
    @AuthRole({"STAFF", "ADMIN"})
    public Mono<ResponseEntity<Map<String, Object>>> getNonPaymentItemClcdList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return apiClient.getNonPaymentItemClcdList(pageNo, numOfRows)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "비급여 진료비 종별 통계 조회 성공",
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "비급여 진료비 종별 통계 조회 실패"
                )));
    }

    @GetMapping("/statistics/by-region")
    @AuthRole({"STAFF", "ADMIN"})
    public Mono<ResponseEntity<Map<String, Object>>> getNonPaymentItemSidoCdList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return apiClient.getNonPaymentItemSidoCdList(pageNo, numOfRows)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "비급여 진료비 지역별 통계 조회 성공",
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "비급여 진료비 지역별 통계 조회 실패"
                )));
    }
}

