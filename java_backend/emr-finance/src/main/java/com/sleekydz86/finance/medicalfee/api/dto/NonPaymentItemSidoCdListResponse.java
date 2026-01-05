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
public class NonPaymentItemSidoCdListResponse {

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
        private List<NonPaymentItemSidoCdItem> item;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NonPaymentItemSidoCdItem {
        @JacksonXmlProperty(localName = "npayCd")
        private String npayCd;
        @JacksonXmlProperty(localName = "npayKorNm")
        private String npayKorNm;
        @JacksonXmlProperty(localName = "stdDate")
        private String stdDate;
        @JacksonXmlProperty(localName = "prcMaxAll")
        private Long prcMaxAll;
        @JacksonXmlProperty(localName = "prcMaxSl")
        private Long prcMaxSl;
        @JacksonXmlProperty(localName = "prcMaxPs")
        private Long prcMaxPs;
        @JacksonXmlProperty(localName = "prcMaxIch")
        private Long prcMaxIch;
        @JacksonXmlProperty(localName = "prcMaxTg")
        private Long prcMaxTg;
        @JacksonXmlProperty(localName = "prcMaxKw")
        private Long prcMaxKw;
        @JacksonXmlProperty(localName = "prcMaxDj")
        private Long prcMaxDj;
        @JacksonXmlProperty(localName = "prcMaxUsn")
        private Long prcMaxUsn;
        @JacksonXmlProperty(localName = "prcMaxKyg")
        private Long prcMaxKyg;
        @JacksonXmlProperty(localName = "prcMaxKaw")
        private Long prcMaxKaw;
        @JacksonXmlProperty(localName = "prcMaxCcbk")
        private Long prcMaxCcbk;
        @JacksonXmlProperty(localName = "prcMaxCcn")
        private Long prcMaxCcn;
        @JacksonXmlProperty(localName = "prcMaxClb")
        private Long prcMaxClb;
        @JacksonXmlProperty(localName = "prcMaxCln")
        private Long prcMaxCln;
        @JacksonXmlProperty(localName = "prcMaxKsb")
        private Long prcMaxKsb;
        @JacksonXmlProperty(localName = "prcMaxKsn")
        private Long prcMaxKsn;
        @JacksonXmlProperty(localName = "prcMaxChj")
        private Long prcMaxChj;
        @JacksonXmlProperty(localName = "prcMaxSejong")
        private Long prcMaxSejong;
        @JacksonXmlProperty(localName = "prcMinAll")
        private Long prcMinAll;
        @JacksonXmlProperty(localName = "prcMinSl")
        private Long prcMinSl;
        @JacksonXmlProperty(localName = "prcMinPs")
        private Long prcMinPs;
        @JacksonXmlProperty(localName = "prcMinIch")
        private Long prcMinIch;
        @JacksonXmlProperty(localName = "prcMinTg")
        private Long prcMinTg;
        @JacksonXmlProperty(localName = "prcMinKw")
        private Long prcMinKw;
        @JacksonXmlProperty(localName = "prcMinDj")
        private Long prcMinDj;
        @JacksonXmlProperty(localName = "prcMinUsn")
        private Long prcMinUsn;
        @JacksonXmlProperty(localName = "prcMinKyg")
        private Long prcMinKyg;
        @JacksonXmlProperty(localName = "prcMinKaw")
        private Long prcMinKaw;
        @JacksonXmlProperty(localName = "prcMinCcbk")
        private Long prcMinCcbk;
        @JacksonXmlProperty(localName = "prcMinCcn")
        private Long prcMinCcn;
        @JacksonXmlProperty(localName = "prcMinClb")
        private Long prcMinClb;
        @JacksonXmlProperty(localName = "prcMinCln")
        private Long prcMinCln;
        @JacksonXmlProperty(localName = "prcMinKsb")
        private Long prcMinKsb;
        @JacksonXmlProperty(localName = "prcMinKsn")
        private Long prcMinKsn;
        @JacksonXmlProperty(localName = "prcMinChj")
        private Long prcMinChj;
        @JacksonXmlProperty(localName = "prcMinSejong")
        private Long prcMinSejong;
        @JacksonXmlProperty(localName = "prcAvgAll")
        private Long prcAvgAll;
        @JacksonXmlProperty(localName = "prcAvgSl")
        private Long prcAvgSl;
        @JacksonXmlProperty(localName = "prcAvgPs")
        private Long prcAvgPs;
        @JacksonXmlProperty(localName = "prcAvgIch")
        private Long prcAvgIch;
        @JacksonXmlProperty(localName = "prcAvgTg")
        private Long prcAvgTg;
        @JacksonXmlProperty(localName = "prcAvgKw")
        private Long prcAvgKw;
        @JacksonXmlProperty(localName = "prcAvgDj")
        private Long prcAvgDj;
        @JacksonXmlProperty(localName = "prcAvgUsn")
        private Long prcAvgUsn;
        @JacksonXmlProperty(localName = "prcAvgKyg")
        private Long prcAvgKyg;
        @JacksonXmlProperty(localName = "prcAvgKaw")
        private Long prcAvgKaw;
        @JacksonXmlProperty(localName = "prcAvgCcbk")
        private Long prcAvgCcbk;
        @JacksonXmlProperty(localName = "prcAvgCcn")
        private Long prcAvgCcn;
        @JacksonXmlProperty(localName = "prcAvgClb")
        private Long prcAvgClb;
        @JacksonXmlProperty(localName = "prcAvgCln")
        private Long prcAvgCln;
        @JacksonXmlProperty(localName = "prcAvgKsb")
        private Long prcAvgKsb;
        @JacksonXmlProperty(localName = "prcAvgKsn")
        private Long prcAvgKsn;
        @JacksonXmlProperty(localName = "prcAvgChj")
        private Long prcAvgChj;
        @JacksonXmlProperty(localName = "prcAvgSejong")
        private Long prcAvgSejong;
        @JacksonXmlProperty(localName = "middAvgAll")
        private Long middAvgAll;
        @JacksonXmlProperty(localName = "middAvgSl")
        private Long middAvgSl;
        @JacksonXmlProperty(localName = "middAvgPs")
        private Long middAvgPs;
        @JacksonXmlProperty(localName = "middAvgIch")
        private Long middAvgIch;
        @JacksonXmlProperty(localName = "middAvgTg")
        private Long middAvgTg;
        @JacksonXmlProperty(localName = "middAvgKw")
        private Long middAvgKw;
        @JacksonXmlProperty(localName = "middAvgDj")
        private Long middAvgDj;
        @JacksonXmlProperty(localName = "middAvgUsn")
        private Long middAvgUsn;
        @JacksonXmlProperty(localName = "middAvgKyg")
        private Long middAvgKyg;
        @JacksonXmlProperty(localName = "middAvgKaw")
        private Long middAvgKaw;
        @JacksonXmlProperty(localName = "middAvgCcbk")
        private Long middAvgCcbk;
        @JacksonXmlProperty(localName = "middAvgCcn")
        private Long middAvgCcn;
        @JacksonXmlProperty(localName = "middAvgClb")
        private Long middAvgClb;
        @JacksonXmlProperty(localName = "middAvgCln")
        private Long middAvgCln;
        @JacksonXmlProperty(localName = "middAvgKsb")
        private Long middAvgKsb;
        @JacksonXmlProperty(localName = "middAvgKsn")
        private Long middAvgKsn;
        @JacksonXmlProperty(localName = "middAvgChj")
        private Long middAvgChj;
        @JacksonXmlProperty(localName = "middAvgSejong")
        private Long middAvgSejong;
    }

    public List<NonPaymentItemSidoCdItem> getItems() {
        if (body != null && body.getItems() != null && body.getItems().getItem() != null) {
            return body.getItems().getItem();
        }
        return List.of();
    }

    public int getTotalCount() {
        return body != null ? body.getTotalCount() : 0;
    }
}

