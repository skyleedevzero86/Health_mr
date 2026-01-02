package com.sleekydz86.emrclinical.treatment.emergency;

import com.sleekydz86.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyTreatmentRepository extends BaseRepository<EmergencyTreatmentEntity, Long> {

    EmergencyTreatmentEntity findByTreatmentId_TreatmentId(Long treatmentId);

    List<EmergencyTreatmentEntity> findByCheckInEntity_CheckInId(Long checkInId);
}