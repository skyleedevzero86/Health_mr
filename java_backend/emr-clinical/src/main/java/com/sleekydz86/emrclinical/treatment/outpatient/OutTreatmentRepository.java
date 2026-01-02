package com.sleekydz86.emrclinical.treatment.outpatient;

import com.sleekydz86.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutTreatmentRepository extends BaseRepository<OutTreatmentEntity, Long> {
    OutTreatmentEntity findByTreatmentId_TreatmentId(Long treatmentId);

    List<OutTreatmentEntity> findByCheckInEntity_CheckInId(Long checkInId);
}

