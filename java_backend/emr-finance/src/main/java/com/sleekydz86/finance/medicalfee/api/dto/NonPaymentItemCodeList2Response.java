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
public class NonPaymentItemCodeList2Response {

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
        private List<NonPaymentItemCodeItem> item;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NonPaymentItemCodeItem {
        @JacksonXmlProperty(localName = "npayCd")
        private String npayCd;
        @JacksonXmlProperty(localName = "npayKorNm")
        private String npayKorNm;
        @JacksonXmlProperty(localName = "npayMdivCd")
        private String npayMdivCd;
        @JacksonXmlProperty(localName = "npayMdivCdNm")
        private String npayMdivCdNm;
        @JacksonXmlProperty(localName = "npaySdivCd")
        private String npaySdivCd;
        @JacksonXmlProperty(localName = "npaySdivCdNm")
        private String npaySdivCdNm;
        @JacksonXmlProperty(localName = "npayDtlDivCd")
        private String npayDtlDivCd;
        @JacksonXmlProperty(localName = "npayDtlDivCdNm")
        private String npayDtlDivCdNm;
        @JacksonXmlProperty(localName = "cmmtTxt")
        private String cmmtTxt;
        @JacksonXmlProperty(localName = "adtFrDd")
        private String adtFrDd;
        @JacksonXmlProperty(localName = "adtEndDd")
        private String adtEndDd;
    }

    public List<NonPaymentItemCodeItem> getItems() {
        if (body != null && body.getItems() != null && body.getItems().getItem() != null) {
            return body.getItems().getItem();
        }
        return List.of();
    }

    public int getTotalCount() {
        return body != null ? body.getTotalCount() : 0;
    }
}

