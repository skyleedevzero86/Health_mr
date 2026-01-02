package com.sleekydz86.emrclinical.treatment.inpatient;

import com.sleekydz86.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InTreatmentRepository extends BaseRepository<InTreatmentEntity, Long> {

    InTreatmentEntity findByTreatmentId_TreatmentId(Long treatmentId);

    List<InTreatmentEntity> findByCheckInEntity_CheckInId(Long checkInId);
}
