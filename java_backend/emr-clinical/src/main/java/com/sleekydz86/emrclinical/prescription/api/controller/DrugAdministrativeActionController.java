package com.sleekydz86.emrclinical.prescription.api.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionResponse;
import com.sleekydz86.emrclinical.prescription.service.DrugAdministrativeActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prescription/drug-admin-action")
@RequiredArgsConstructor
public class DrugAdministrativeActionController {
    
    private final DrugAdministrativeActionService adminActionService;
    
    @GetMapping("/item/{itemSeq}")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getActionsByItemSeq(
            @PathVariable String itemSeq) {
        AdministrativeActionResponse response = adminActionService.checkAdministrativeAction(itemSeq);
        
        return ResponseEntity.ok(Map.of(
                "message", "행정처분 정보 조회 성공",
                "itemSeq", itemSeq,
                "data", response != null ? response : Map.of()
        ));
    }
    
    @GetMapping("/item/{itemSeq}/check")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> checkActiveAction(
            @PathVariable String itemSeq) {
        boolean hasActive = adminActionService.hasActiveAction(itemSeq);
        
        return ResponseEntity.ok(Map.of(
                "itemSeq", itemSeq,
                "hasActiveAction", hasActive,
                "message", hasActive ? "유효한 행정처분이 있습니다" : "유효한 행정처분이 없습니다"
        ));
    }
}

