package com.sleekydz86.emrclinical.prescription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugInfoItemResponse {
    
    @JsonProperty("entpName")
    private String entpName;              // 업체명
    
    @JsonProperty("itemSeq")
    private String itemSeq;               // 품목기준코드
    
    @JsonProperty("itemName")
    private String itemName;              // 제품명
    
    @JsonProperty("efcyQesitm")
    private String efcyQesitm;            // 효능
    
    @JsonProperty("useMethodQesitm")
    private String useMethodQesitm;       // 사용법
    
    @JsonProperty("atpnWarnQesitm")
    private String atpnWarnQesitm;         // 주의사항 경고
    
    @JsonProperty("atpnQesitm")
    private String atpnQesitm;             // 주의사항
    
    @JsonProperty("intrcQesitm")
    private String intrcQesitm;            // 상호작용
    
    @JsonProperty("seQesitm")
    private String seQesitm;               // 부작용
    
    @JsonProperty("depositMethodQesitm")
    private String depositMethodQesitm;    // 보관법
    
    @JsonProperty("openDe")
    private String openDe;                 // 공개일자
    
    @JsonProperty("updateDe")
    private String updateDe;                // 수정일자
    
    @JsonProperty("itemImage")
    private String itemImage;              // 낱알 이미지 URL
}

