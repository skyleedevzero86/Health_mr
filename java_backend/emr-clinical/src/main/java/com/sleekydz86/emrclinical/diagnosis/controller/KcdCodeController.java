package com.sleekydz86.emrclinical.diagnosis.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.diagnosis.dto.KcdCodeResponse;
import com.sleekydz86.emrclinical.diagnosis.entity.KcdCodeEntity;
import com.sleekydz86.emrclinical.diagnosis.service.KcdCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis/kcd")
@RequiredArgsConstructor
public class KcdCodeController {

    private final KcdCodeService kcdCodeService;

    @GetMapping("/search")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Page<KcdCodeResponse>> searchKcdCodes(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<KcdCodeEntity> results = kcdCodeService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(results.map(KcdCodeResponse::from));
    }

    @GetMapping("/{code}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<KcdCodeResponse> getKcdCode(@PathVariable String code) {
        KcdCodeEntity entity = kcdCodeService.findByCode(code);
        return ResponseEntity.ok(KcdCodeResponse.from(entity));
    }

    @GetMapping("/category/{category}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Page<KcdCodeResponse>> getByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<KcdCodeEntity> results = kcdCodeService.findByCategory(category, pageable);
        return ResponseEntity.ok(results.map(KcdCodeResponse::from));
    }
}
