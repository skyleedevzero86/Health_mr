package com.sleekydz86.finance.medicalfee.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NonPaymentItemCodeRequest {
    @Builder.Default
    private int pageNo = 1;
    @Builder.Default
    private int numOfRows = 10;
    private String searchKeyword;
    private String npayMdivCd;
    private String npaySdivCd;
}

