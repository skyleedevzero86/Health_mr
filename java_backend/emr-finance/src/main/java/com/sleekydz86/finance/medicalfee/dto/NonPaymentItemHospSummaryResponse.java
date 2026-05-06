package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class NonPaymentItemHospSummaryResponse {
    private String ykiho;
    private String yadmNm;
    private String clCd;
    private String clCdNm;
    private String sidoCd;
    private String sidoCdNm;
    private String sgguCd;
    private String sgguCdNm;
    private String npayCd;
    private String npayKorNm;
    private LocalDate adtFrDd;
    private LocalDate adtEndDd;
    private Long minPrc;
    private Long maxPrc;
    private Long avgPrc;
}

