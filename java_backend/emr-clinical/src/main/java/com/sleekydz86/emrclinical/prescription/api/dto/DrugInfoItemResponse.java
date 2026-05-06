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
    private String entpName;
    
    @JsonProperty("itemSeq")
    private String itemSeq;
    
    @JsonProperty("itemName")
    private String itemName;
    
    @JsonProperty("efcyQesitm")
    private String efcyQesitm;
    
    @JsonProperty("useMethodQesitm")
    private String useMethodQesitm;
    
    @JsonProperty("atpnWarnQesitm")
    private String atpnWarnQesitm;
    
    @JsonProperty("atpnQesitm")
    private String atpnQesitm;
    
    @JsonProperty("intrcQesitm")
    private String intrcQesitm;
    
    @JsonProperty("seQesitm")
    private String seQesitm;
    
    @JsonProperty("depositMethodQesitm")
    private String depositMethodQesitm;
    
    @JsonProperty("openDe")
    private String openDe;
    
    @JsonProperty("updateDe")
    private String updateDe;
    
    @JsonProperty("itemImage")
    private String itemImage;
}

