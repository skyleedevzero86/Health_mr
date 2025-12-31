package com.sleekydz86.core.common.exception.custom;

import com.sleekydz86.core.common.exception.ErrorCode;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(ErrorCode.NOT_FOUND, message, cause);
    }
}

