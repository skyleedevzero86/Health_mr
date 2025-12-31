package com.sleekydz86.core.common.exception.custom;

import com.sleekydz86.core.common.exception.ErrorCode;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    public BusinessException(String message, Throwable cause) {
        super(ErrorCode.BUSINESS_ERROR, message, cause);
    }
}