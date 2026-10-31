package com.sleekydz86.emrclinical.checkin.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.emrclinical.checkin.dto.CheckInCreateRequest;
import com.sleekydz86.emrclinical.checkin.dto.CheckInResponse;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/check-in")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/register")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ResponseEntity<CheckInResponse> registerCheckIn(
            @Valid @RequestBody CheckInCreateRequest request,
            @AuthUser Long userId) {
        Long resolvedUserId = request.getUserId() != null ? request.getUserId() : userId;
        CheckInEntity checkIn = checkInService.createCheckIn(request.getPatientNo(), resolvedUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(CheckInResponse.from(checkIn));
    }

    @GetMapping("/{checkInId}")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<CheckInResponse> getCheckIn(@PathVariable Long checkInId) {
        return ResponseEntity.ok(CheckInResponse.from(checkInService.getCheckInById(checkInId)));
    }

    @GetMapping
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<List<CheckInResponse>> getCheckIns(@RequestParam(required = false) Long patientNo) {
        List<CheckInEntity> checkIns = patientNo != null
                ? checkInService.getCheckInsByPatientNo(patientNo)
                : List.of();
        return ResponseEntity.ok(checkIns.stream().map(CheckInResponse::from).toList());
    }

    @PostMapping("/{checkInId}/complete")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<CheckInResponse> completeCheckIn(@PathVariable Long checkInId) {
        return ResponseEntity.ok(CheckInResponse.from(checkInService.completeCheckIn(checkInId)));
    }

    @PostMapping("/{checkInId}/cancel")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<CheckInResponse> cancelCheckIn(@PathVariable Long checkInId) {
        return ResponseEntity.ok(CheckInResponse.from(checkInService.cancelCheckIn(checkInId)));
    }
}
