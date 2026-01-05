package com.sleekydz86.emrclinical.prescription.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DrugInfoSearchRequest {
    private String itemName;      // 제품명
    private String itemSeq;       // 품목기준코드
    private String entpName;      // 업체명
    private Integer pageNo;       // 페이지 번호
    private Integer numOfRows;   // 한 페이지 결과수
    
    public DrugInfoSearchRequest() {
        this.pageNo = 1;
        this.numOfRows = 10;
    }
    
    public DrugInfoSearchRequest(String itemName, String itemSeq, String entpName, Integer pageNo, Integer numOfRows) {
        this.itemName = itemName;
        this.itemSeq = itemSeq;
        this.entpName = entpName;
        this.pageNo = pageNo != null ? pageNo : 1;
        this.numOfRows = numOfRows != null ? numOfRows : 10;
    }
}

