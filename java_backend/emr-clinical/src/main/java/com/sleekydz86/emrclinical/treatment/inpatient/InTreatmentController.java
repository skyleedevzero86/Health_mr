package com.sleekydz86.emrclinical.treatment.inpatient;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.user.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/treatment/inpatient")
@RequiredArgsConstructor
public class InTreatmentController {

    private final InTreatmentRepository inTreatmentRepository;

    @GetMapping("/treatment/{treatmentId}")
    @AuthRole({ RoleType.DOCTOR, RoleType.ADMIN, RoleType.STAFF })
    public ResponseEntity<InTreatmentEntity> getInTreatmentByTreatmentId(
            @PathVariable Long treatmentId) {
        InTreatmentEntity inTreatment = inTreatmentRepository.findByTreatmentId_TreatmentId(treatmentId);
        if (inTreatment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inTreatment);
    }

    @GetMapping("/checkin/{checkInId}")
    @AuthRole({ RoleType.DOCTOR, RoleType.ADMIN, RoleType.STAFF })
    public ResponseEntity<List<InTreatmentEntity>> getInTreatmentsByCheckInId(
            @PathVariable Long checkInId) {
        List<InTreatmentEntity> inTreatments = inTreatmentRepository.findByCheckInEntity_CheckInId(checkInId);
        return ResponseEntity.ok(inTreatments);
    }
}

