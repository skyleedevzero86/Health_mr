package com.sleekydz86.core.common.exception.handler;

import com.sleekydz86.core.common.exception.ErrorCode;
import com.sleekydz86.core.common.exception.custom.BaseException;
import com.sleekydz86.core.security.jwt.CustomAuthenticationException;
import com.sleekydz86.core.security.jwt.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<CustomErrorResponse> handleBaseException(
            BaseException e, HttpServletRequest request) {
        log.error("BaseException 발생: {}", e.getMessage(), e);
        CustomErrorResponse response = CustomErrorResponse.of(
                e.getErrorCode().getStatus(),
                e.getMessage(),
                e.getErrorCode().name(),
                request.getRequestURI()
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<CustomErrorResponse> handleCustomAuthenticationException(
            CustomAuthenticationException e, HttpServletRequest request) {
        log.error("CustomAuthenticationException 발생: {}", e.getMessage(), e);
        CustomErrorResponse response = CustomErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getStatus(),
                e.getMessage(),
                ErrorCode.UNAUTHORIZED.name(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomErrorResponse> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        log.error("AccessDeniedException 발생: {}", e.getMessage(), e);
        CustomErrorResponse response = CustomErrorResponse.of(
                ErrorCode.ACCESS_FORBIDDEN.getStatus(),
                ErrorCode.ACCESS_FORBIDDEN.getMessage(),
                ErrorCode.ACCESS_FORBIDDEN.name(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.ACCESS_FORBIDDEN.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("MethodArgumentNotValidException 발생: {}", e.getMessage(), e);

        Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "유효하지 않은 값",
                        (existing, replacement) -> existing
                ));

        CustomErrorResponse response = CustomErrorResponse.of(
                ErrorCode.VALIDATION_ERROR.getStatus(),
                ErrorCode.VALIDATION_ERROR.getMessage(),
                ErrorCode.VALIDATION_ERROR.name(),
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("HttpRequestMethodNotSupportedException 발생: {}", e.getMessage(), e);
        CustomErrorResponse response = CustomErrorResponse.of(
                ErrorCode.BAD_REQUEST.getStatus(),
                "지원하지 않는 HTTP 메서드입니다: " + e.getMethod(),
                ErrorCode.BAD_REQUEST.name(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);

        String stackTrace = null;
        if (isDevelopment()) {
            stackTrace = getStackTrace(e);
        }

        CustomErrorResponse response = CustomErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                request.getRequestURI(),
                stackTrace
        );
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(response);
    }

    private boolean isDevelopment() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return true; // 기본값은 개발 환경
        }
        for (String profile : activeProfiles) {
            if (profile.equals("dev") || profile.equals("local")) {
                return true;
            }
        }
        return false;
    }

    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}

