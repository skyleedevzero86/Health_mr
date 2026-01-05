package com.sleekydz86.finance.medicalfee.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class NonPaymentItemClcdListResponse {

    @JacksonXmlProperty(localName = "header")
    private Header header;

    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;
        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        @JacksonXmlProperty(localName = "items")
        @JacksonXmlElementWrapper(localName = "items")
        private Items items;
        @JacksonXmlProperty(localName = "numOfRows")
        private int numOfRows;
        @JacksonXmlProperty(localName = "pageNo")
        private int pageNo;
        @JacksonXmlProperty(localName = "totalCount")
        private int totalCount;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Items {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<NonPaymentItemClcdItem> item;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NonPaymentItemClcdItem {
        @JacksonXmlProperty(localName = "npayCd")
        private String npayCd;
        @JacksonXmlProperty(localName = "npayKorNm")
        private String npayKorNm;
        @JacksonXmlProperty(localName = "stdDate")
        private String stdDate;
        @JacksonXmlProperty(localName = "prcMaxAll")
        private Long prcMaxAll;
        @JacksonXmlProperty(localName = "prcMaxUsgh")
        private Long prcMaxUsgh;
        @JacksonXmlProperty(localName = "prcMaxGnhp")
        private Long prcMaxGnhp;
        @JacksonXmlProperty(localName = "prcMaxDety")
        private Long prcMaxDety;
        @JacksonXmlProperty(localName = "prcMaxCmdc")
        private Long prcMaxCmdc;
        @JacksonXmlProperty(localName = "prcMaxHosp")
        private Long prcMaxHosp;
        @JacksonXmlProperty(localName = "prcMaxRecu")
        private Long prcMaxRecu;
        @JacksonXmlProperty(localName = "prcMinAll")
        private Long prcMinAll;
        @JacksonXmlProperty(localName = "prcMinUsgh")
        private Long prcMinUsgh;
        @JacksonXmlProperty(localName = "prcMinGnhp")
        private Long prcMinGnhp;
        @JacksonXmlProperty(localName = "prcMinDety")
        private Long prcMinDety;
        @JacksonXmlProperty(localName = "prcMinCmdc")
        private Long prcMinCmdc;
        @JacksonXmlProperty(localName = "prcMinHosp")
        private Long prcMinHosp;
        @JacksonXmlProperty(localName = "prcMinRecu")
        private Long prcMinRecu;
        @JacksonXmlProperty(localName = "prcAvgAll")
        private Long prcAvgAll;
        @JacksonXmlProperty(localName = "prcAvgUsgh")
        private Long prcAvgUsgh;
        @JacksonXmlProperty(localName = "prcAvgGnhp")
        private Long prcAvgGnhp;
        @JacksonXmlProperty(localName = "prcAvgDety")
        private Long prcAvgDety;
        @JacksonXmlProperty(localName = "prcAvgCmdc")
        private Long prcAvgCmdc;
        @JacksonXmlProperty(localName = "prcAvgHosp")
        private Long prcAvgHosp;
        @JacksonXmlProperty(localName = "prcAvgRecu")
        private Long prcAvgRecu;
        @JacksonXmlProperty(localName = "middAvgAll")
        private Long middAvgAll;
        @JacksonXmlProperty(localName = "middAvgUsgh")
        private Long middAvgUsgh;
        @JacksonXmlProperty(localName = "middAvgGnhp")
        private Long middAvgGnhp;
        @JacksonXmlProperty(localName = "middAvgDety")
        private Long middAvgDety;
        @JacksonXmlProperty(localName = "middAvgCmdc")
        private Long middAvgCmdc;
        @JacksonXmlProperty(localName = "middAvgHosp")
        private Long middAvgHosp;
        @JacksonXmlProperty(localName = "middAvgRecu")
        private Long middAvgRecu;
    }

    public List<NonPaymentItemClcdItem> getItems() {
        if (body != null && body.getItems() != null && body.getItems().getItem() != null) {
            return body.getItems().getItem();
        }
        return List.of();
    }

    public int getTotalCount() {
        return body != null ? body.getTotalCount() : 0;
    }
}

