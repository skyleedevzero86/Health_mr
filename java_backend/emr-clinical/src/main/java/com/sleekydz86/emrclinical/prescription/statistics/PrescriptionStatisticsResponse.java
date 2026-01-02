package com.sleekydz86.emrclinical.prescription.statistics;

import com.sleekydz86.emrclinical.types.PrescriptionType;
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
public class PrescriptionStatisticsResponse {

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long doctorId;
    private Long totalCount;
    private Long prescribedCount;
    private Long dispensedCount;
    private Long pendingCount;
    private Long cancelledCount;
    private Map<PrescriptionType, Long> typeCount;
}

