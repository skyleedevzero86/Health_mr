package com.sleekydz86.emrclinical.treatment.emergency;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.user.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/treatment/emergency")
@RequiredArgsConstructor
public class EmergencyTreatmentController {

    private final EmergencyTreatmentRepository emergencyTreatmentRepository;

    @GetMapping("/treatment/{treatmentId}")
    @AuthRole({ RoleType.DOCTOR, RoleType.ADMIN, RoleType.STAFF })
    public ResponseEntity<EmergencyTreatmentEntity> getEmergencyTreatmentByTreatmentId(
            @PathVariable Long treatmentId) {
        EmergencyTreatmentEntity emergencyTreatment = emergencyTreatmentRepository.findByTreatmentId_TreatmentId(treatmentId);
        if (emergencyTreatment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(emergencyTreatment);
    }

    @GetMapping("/checkin/{checkInId}")
    @AuthRole({ RoleType.DOCTOR, RoleType.ADMIN, RoleType.STAFF })
    public ResponseEntity<List<EmergencyTreatmentEntity>> getEmergencyTreatmentsByCheckInId(
            @PathVariable Long checkInId) {
        List<EmergencyTreatmentEntity> emergencyTreatments = emergencyTreatmentRepository.findByCheckInEntity_CheckInId(checkInId);
        return ResponseEntity.ok(emergencyTreatments);
    }
}

