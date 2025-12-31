package com.sleekydz86.core.common.exception.custom;

import com.sleekydz86.core.common.exception.ErrorCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(ErrorCode.ACCESS_FORBIDDEN, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(ErrorCode.ACCESS_FORBIDDEN, message, cause);
    }
}
