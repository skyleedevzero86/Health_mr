package com.sleekydz86.emrclinical.prescription.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DrugInfoSearchRequest {
    private String itemName;
    private String itemSeq;
    private String entpName;
    private Integer pageNo;
    private Integer numOfRows;
    
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

