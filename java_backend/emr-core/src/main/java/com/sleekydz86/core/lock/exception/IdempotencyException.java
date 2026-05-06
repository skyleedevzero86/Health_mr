package com.sleekydz86.core.lock.exception;

import com.sleekydz86.core.common.exception.ErrorCode;
import com.sleekydz86.core.common.exception.custom.BaseException;

public class IdempotencyException extends BaseException {
    public IdempotencyException(String message) {
        super(ErrorCode.DUPLICATE_ERROR, message);
    }

    public IdempotencyException(String message, Throwable cause) {
        super(ErrorCode.DUPLICATE_ERROR, message, cause);
    }
}
