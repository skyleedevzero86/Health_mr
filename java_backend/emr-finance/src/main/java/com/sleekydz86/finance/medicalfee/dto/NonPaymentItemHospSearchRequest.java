package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NonPaymentItemHospSearchRequest {
    private String npayCd;
    private String ykiho;
    private String clCd;
    private String sidoCd;
    private String sgguCd;
    private String yadmNm;
    private String searchWrd;
    @Builder.Default
    private int pageNo = 1;
    @Builder.Default
    private int numOfRows = 10;
}

