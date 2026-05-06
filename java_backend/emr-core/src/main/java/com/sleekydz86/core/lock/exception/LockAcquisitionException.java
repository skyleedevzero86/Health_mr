package com.sleekydz86.core.lock.exception;

import com.sleekydz86.core.common.exception.ErrorCode;
import com.sleekydz86.core.common.exception.custom.BaseException;

public class LockAcquisitionException extends BaseException {
    public LockAcquisitionException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    public LockAcquisitionException(String message, Throwable cause) {
        super(ErrorCode.BUSINESS_ERROR, message, cause);
    }
}
