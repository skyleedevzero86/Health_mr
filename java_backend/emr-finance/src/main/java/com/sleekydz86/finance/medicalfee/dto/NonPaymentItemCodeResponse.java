package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class NonPaymentItemCodeResponse {
    private String npayCd;
    private String npayKorNm;
    private String npayMdivCd;
    private String npayMdivCdNm;
    private String npaySdivCd;
    private String npaySdivCdNm;
    private String npayDtlDivCd;
    private String npayDtlDivCdNm;
    private String cmmtTxt;
    private LocalDate adtFrDd;
    private LocalDate adtEndDd;
}

