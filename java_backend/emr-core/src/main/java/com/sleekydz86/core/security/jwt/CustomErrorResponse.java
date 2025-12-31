package com.sleekydz86.core.security.jwt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomErrorResponse(
        int status,
        String message,
        String errorCode,
        LocalDateTime timestamp,
        String path,
        Map<String, Object> details,
        String stackTrace
) {
    public static CustomErrorResponse of(int status, String message, String errorCode, String path) {
        return CustomErrorResponse.builder()
                .status(status)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static CustomErrorResponse of(int status, String message, String errorCode, String path, Map<String, Object> details) {
        return CustomErrorResponse.builder()
                .status(status)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(details)
                .build();
    }

    public static CustomErrorResponse of(int status, String message, String errorCode, String path, String stackTrace) {
        return CustomErrorResponse.builder()
                .status(status)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .path(path)
                .stackTrace(stackTrace)
                .build();
    }
}

