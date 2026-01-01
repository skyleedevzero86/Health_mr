package com.sleekydz86.domain.patient.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.domain.user.type.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/register")
    @AuthRole(roles = {RoleType.DOCTOR, RoleType.NURSE, RoleType.ADMIN})
    public ResponseEntity<PatientDetailResponse> registerPatient(
            @Valid @RequestBody PatientRegisterRequest request) {
        PatientEntity patient = patientService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PatientDetailResponse.from(patient));
    }

    @PostMapping("/search")
    public ResponseEntity<List<PatientSearchResponse>> searchPatient(
            @RequestParam("patientName") String patientName) {
        List<PatientEntity> patients = patientService.searchPatientsByName(patientName);
        List<PatientSearchResponse> response = patients.stream()
                .map(PatientSearchResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<List<PatientSearchResponse>> searchPatients(
            @Valid @RequestBody PatientSearchRequest request) {
        List<PatientEntity> patients = patientService.searchPatients(request);
        List<PatientSearchResponse> response = patients.stream()
                .map(PatientSearchResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detail")
    public ResponseEntity<PatientDetailResponse> detailPatient(
            @RequestParam("patientNo") Long patientNo) {
        PatientEntity patient = patientService.getPatientByNo(patientNo);
        return ResponseEntity.ok(PatientDetailResponse.from(patient));
    }

    @GetMapping("/{patientNo}")
    public ResponseEntity<PatientDetailResponse> getPatientByNo(@PathVariable Long patientNo) {
        PatientEntity patient = patientService.getPatientByNo(patientNo);
        return ResponseEntity.ok(PatientDetailResponse.from(patient));
    }

    @GetMapping
    public ResponseEntity<Page<PatientListResponse>> getAllPatients(
            @PageableDefault(size = 20, sort = "patientLastVisit") Pageable pageable) {
        Page<PatientEntity> patients = patientService.getAllPatients(pageable);
        Page<PatientListResponse> response = patients.map(PatientListResponse::from);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{patientNo}")
    @AuthRole(roles = {RoleType.DOCTOR, RoleType.NURSE, RoleType.ADMIN})
    public ResponseEntity<PatientDetailResponse> updatePatient(
            @PathVariable Long patientNo,
            @Valid @RequestBody PatientUpdateRequest request) {
        PatientEntity patient = patientService.updatePatient(patientNo, request);
        return ResponseEntity.ok(PatientDetailResponse.from(patient));
    }

    @DeleteMapping("/{patientNo}")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientNo) {
        patientService.deletePatient(patientNo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PatientListResponse>> getRecentPatients(
            @RequestParam(defaultValue = "7") int days) {
        List<PatientEntity> patients = patientService.getRecentPatients(days);
        List<PatientListResponse> response = patients.stream()
                .map(PatientListResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}

