package com.sleekydz86.core.common.exception.custom;

import com.sleekydz86.core.common.exception.ErrorCode;

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
    }
}

