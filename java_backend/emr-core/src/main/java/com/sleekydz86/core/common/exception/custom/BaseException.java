package com.sleekydz86.core.common.exception.custom;

import com.sleekydz86.core.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.detailMessage = message;
    }

    public BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.detailMessage = message;
    }

    public int getHttpStatus() {
        return errorCode.getStatus();
    }

    @Override
    public String getMessage() {
        return detailMessage != null ? detailMessage : errorCode.getMessage();
    }
}

