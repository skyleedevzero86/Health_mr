package com.sleekydz86.emrclinical.prescription.api.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoItemResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoSearchRequest;
import com.sleekydz86.emrclinical.prescription.service.DrugInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescription/drug-info")
@RequiredArgsConstructor
public class DrugInfoApiController {

    private final DrugInfoService drugInfoService;

    @GetMapping("/search")
    @AuthRole({ "DOCTOR", "ADMIN", "STAFF" })
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<Map<String, Object>> searchDrugInfo(
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemSeq,
            @RequestParam(required = false) String entpName,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer numOfRows) {

        try {
            DrugInfoSearchRequest request = DrugInfoSearchRequest.builder()
                    .itemName(itemName)
                    .itemSeq(itemSeq)
                    .entpName(entpName)
                    .pageNo(pageNo)
                    .numOfRows(numOfRows)
                    .build();

            List<DrugInfoItemResponse> items = drugInfoService.searchDrugInfo(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "검색 성공");
            response.put("data", items);
            response.put("count", items.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "검색 실패");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/item-seq/{itemSeq}")
    @AuthRole({ "DOCTOR", "ADMIN", "STAFF" })
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<Map<String, Object>> getDrugInfoByItemSeq(@PathVariable String itemSeq) {
        try {
            DrugInfoItemResponse drugInfo = drugInfoService.getDrugInfoByItemSeq(itemSeq);

            if (drugInfo == null) {
                Map<String, Object> notFoundResponse = new HashMap<>();
                notFoundResponse.put("message", "의약품 정보를 찾을 수 없습니다.");
                notFoundResponse.put("itemSeq", itemSeq);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "조회 성공");
            response.put("data", drugInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "조회 실패");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/item-name/{itemName}")
    @AuthRole({ "DOCTOR", "ADMIN", "STAFF" })
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<Map<String, Object>> searchDrugInfoByItemName(@PathVariable String itemName) {
        try {
            List<DrugInfoItemResponse> items = drugInfoService.searchDrugInfoByItemName(itemName);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "검색 성공");
            response.put("data", items);
            response.put("count", items.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "검색 실패");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/validate/{drugCode}")
    @AuthRole({ "DOCTOR", "ADMIN", "STAFF" })
    public ResponseEntity<Map<String, Object>> validateDrugCode(@PathVariable String drugCode) {
        try {
            boolean isValid = drugInfoService.validateDrugCode(drugCode);

            Map<String, Object> response = new HashMap<>();
            response.put("drugCode", drugCode);
            response.put("isValid", isValid);
            response.put("message", isValid ? "유효한 약물 코드입니다." : "유효하지 않은 약물 코드입니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "검증 실패");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/check-interactions")
    @AuthRole({ "DOCTOR", "ADMIN" })
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<Map<String, Object>> checkDrugInteractions(
            @Valid @RequestBody List<String> drugCodes) {
        try {
            List<String> interactionWarnings = drugInfoService.checkDrugInteractions(drugCodes);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "상호작용 검사 완료");
            response.put("drugCodes", drugCodes);
            response.put("interactionWarnings", interactionWarnings);
            response.put("hasInteractions", !interactionWarnings.isEmpty());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "상호작용 검사 실패");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}