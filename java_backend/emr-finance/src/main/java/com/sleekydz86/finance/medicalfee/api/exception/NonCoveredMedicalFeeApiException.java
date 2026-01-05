package com.sleekydz86.finance.medicalfee.api.exception;

import lombok.Getter;

@Getter
public class NonCoveredMedicalFeeApiException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public NonCoveredMedicalFeeApiException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public NonCoveredMedicalFeeApiException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static NonCoveredMedicalFeeApiException fromErrorCode(String code) {
        return switch (code) {
            case "03" -> new NonCoveredMedicalFeeApiException("03", "데이터 없음");
            case "10" -> new NonCoveredMedicalFeeApiException("10", "ServiceKey 파라미터 누락");
            case "11" -> new NonCoveredMedicalFeeApiException("11", "필수 요청 파라미터 누락");
            case "20" -> new NonCoveredMedicalFeeApiException("20", "서비스 접근 거부");
            case "22" -> new NonCoveredMedicalFeeApiException("22", "일일 요청 제한 초과");
            case "30" -> new NonCoveredMedicalFeeApiException("30", "등록되지 않은 서비스 키");
            case "31" -> new NonCoveredMedicalFeeApiException("31", "기한 만료된 서비스 키");
            case "32" -> new NonCoveredMedicalFeeApiException("32", "등록되지 않은 도메인명 또는 IP 주소");
            default -> new NonCoveredMedicalFeeApiException(code, "알 수 없는 오류");
        };
    }
}

