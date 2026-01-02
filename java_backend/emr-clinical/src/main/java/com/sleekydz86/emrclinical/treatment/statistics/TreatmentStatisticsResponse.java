package com.sleekydz86.emrclinical.treatment.statistics;

import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentStatisticsResponse {

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long doctorId;
    private Long totalCount;
    private Long completedCount;
    private Long pendingCount;
    private Long inProgressCount;
    private Long cancelledCount;
    private Map<TreatmentType, Long> typeCount;
}

