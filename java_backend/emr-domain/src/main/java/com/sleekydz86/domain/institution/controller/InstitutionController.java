package com.sleekydz86.domain.institution.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.institution.dto.InstitutionCreateRequest;
import com.sleekydz86.domain.institution.dto.InstitutionResponse;
import com.sleekydz86.domain.institution.dto.InstitutionUpdateRequest;
import com.sleekydz86.domain.institution.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @PostMapping
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<InstitutionResponse> create(@RequestBody InstitutionCreateRequest request) {
        InstitutionResponse response = institutionService.create(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{institutionCode}")
    @AuthRole
    public ResponseEntity<InstitutionResponse> findByCode(@PathVariable String institutionCode) {
        InstitutionResponse response = institutionService.findByCode(institutionCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{institutionId}")
    @AuthRole
    public ResponseEntity<InstitutionResponse> findById(@PathVariable Long institutionId) {
        InstitutionResponse response = institutionService.findById(institutionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @AuthRole
    public ResponseEntity<List<InstitutionResponse>> findAll() {
        List<InstitutionResponse> responses = institutionService.findAllActive();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<List<InstitutionResponse>> findAllForAdmin() {
        List<InstitutionResponse> responses = institutionService.findAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{institutionId}")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<InstitutionResponse> update(
            @PathVariable Long institutionId,
            @RequestBody InstitutionUpdateRequest request
    ) {
        InstitutionResponse response = institutionService.update(institutionId, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{institutionId}/activate")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<InstitutionResponse> activate(@PathVariable Long institutionId) {
        InstitutionResponse response = institutionService.activate(institutionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{institutionId}/deactivate")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<InstitutionResponse> deactivate(@PathVariable Long institutionId) {
        InstitutionResponse response = institutionService.deactivate(institutionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{institutionId}")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Long institutionId) {
        institutionService.delete(institutionId);
        return ResponseEntity.noContent().build();
    }
}

