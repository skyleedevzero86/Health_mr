package com.sleekydz86.core.common.exception.custom;

import com.sleekydz86.core.common.exception.ErrorCode;

public class DuplicateException extends BaseException {
    public DuplicateException(String message) {
        super(ErrorCode.DUPLICATE_ERROR, message);
    }

    public DuplicateException(String message, Throwable cause) {
        super(ErrorCode.DUPLICATE_ERROR, message, cause);
    }
}
