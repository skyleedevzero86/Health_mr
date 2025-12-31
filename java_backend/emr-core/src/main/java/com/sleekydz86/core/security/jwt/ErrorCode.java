package com.sleekydz86.core.security.jwt;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다."),
    ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "엑세스 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청한 리소스를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST.value(), "입력값 검증에 실패했습니다."),
    DUPLICATE_ERROR(HttpStatus.CONFLICT.value(), "중복된 데이터입니다."),
    BUSINESS_ERROR(HttpStatus.BAD_REQUEST.value(), "비즈니스 로직 오류가 발생했습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS.value(), "요청 한도를 초과했습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}