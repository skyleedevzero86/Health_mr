package com.sleekydz86.finance.medicalfee.api.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.finance.medicalfee.dto.NonPaymentItemCodeRequest;
import com.sleekydz86.finance.medicalfee.dto.NonPaymentItemHospSearchRequest;
import com.sleekydz86.finance.medicalfee.service.NonCoveredMedicalFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/non-covered-medical-fee")
@RequiredArgsConstructor
public class NonCoveredMedicalFeeApiController {

    private final NonCoveredMedicalFeeService service;

    @GetMapping("/codes")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public Mono<ResponseEntity<Map<String, Object>>> getNonPaymentItemCodes(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String npayMdivCd,
            @RequestParam(required = false) String npaySdivCd
    ) {
        NonPaymentItemCodeRequest request = NonPaymentItemCodeRequest.builder()
                .pageNo(pageNo)
                .numOfRows(numOfRows)
                .searchKeyword(searchKeyword)
                .npayMdivCd(npayMdivCd)
                .npaySdivCd(npaySdivCd)
                .build();

        return service.getNonPaymentItemCodes(request)
                .map(data -> ResponseEntity.ok(Map.of(
                        "message", "비급여 항목 코드 조회 성공",
                        "data", data
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "비급여 항목 코드 조회 실패"
                )));
    }

    @GetMapping("/hospitals/detail")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public Mono<ResponseEntity<Map<String, Object>>> getHospitalDetail(
            @RequestParam(required = false) String ykiho,
            @RequestParam(required = false) String clCd,
            @RequestParam(required = false) String sidoCd,
            @RequestParam(required = false) String sgguCd,
            @RequestParam(required = false) String yadmNm,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        NonPaymentItemHospSearchRequest request = NonPaymentItemHospSearchRequest.builder()
                .ykiho(ykiho)
                .clCd(clCd)
                .sidoCd(sidoCd)
                .sgguCd(sgguCd)
                .yadmNm(yadmNm)
                .pageNo(pageNo)
                .numOfRows(numOfRows)
                .build();

        return service.getNonPaymentItemHospDetail(request)
                .map(data -> ResponseEntity.ok(Map.of(
                        "message", "병원별 상세 정보 조회 성공",
                        "data", data
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "병원별 상세 정보 조회 실패"
                )));
    }

    @GetMapping("/hospitals/summary")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public Mono<ResponseEntity<Map<String, Object>>> getHospitalSummary(
            @RequestParam String npayCd,
            @RequestParam(required = false) String clCd,
            @RequestParam(required = false) String sidoCd,
            @RequestParam(required = false) String sgguCd,
            @RequestParam(required = false) String yadmNm,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        NonPaymentItemHospSearchRequest request = NonPaymentItemHospSearchRequest.builder()
                .npayCd(npayCd)
                .clCd(clCd)
                .sidoCd(sidoCd)
                .sgguCd(sgguCd)
                .yadmNm(yadmNm)
                .pageNo(pageNo)
                .numOfRows(numOfRows)
                .build();

        return service.getNonPaymentItemHospSummary(request)
                .map(data -> ResponseEntity.ok(Map.of(
                        "message", "병원별 요약 정보 조회 성공",
                        "data", data
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "병원별 요약 정보 조회 실패"
                )));
    }

    @GetMapping("/hospitals/search")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public Mono<ResponseEntity<Map<String, Object>>> searchHospitalsByItem(
            @RequestParam String npayCd,
            @RequestParam(required = false) String sidoCd,
            @RequestParam(required = false) String clCd
    ) {
        return service.searchHospitalsByItem(npayCd, sidoCd, clCd)
                .map(items -> ResponseEntity.ok(Map.of(
                        "message", "비급여 항목으로 병원 검색 성공",
                        "data", Map.of("items", items)
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "비급여 항목으로 병원 검색 실패"
                )));
    }
}

