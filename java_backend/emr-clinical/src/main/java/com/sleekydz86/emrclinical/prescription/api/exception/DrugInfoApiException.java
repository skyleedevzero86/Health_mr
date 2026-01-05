package com.sleekydz86.emrclinical.prescription.api.exception;

import com.sleekydz86.core.common.exception.custom.BusinessException;

public class DrugInfoApiException extends BusinessException {
    
    public DrugInfoApiException(String message) {
        super(message);
    }
    
    public DrugInfoApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

