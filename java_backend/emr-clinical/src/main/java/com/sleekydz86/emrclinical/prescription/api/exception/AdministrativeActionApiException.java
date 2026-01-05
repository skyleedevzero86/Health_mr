package com.sleekydz86.emrclinical.prescription.api.exception;

import com.sleekydz86.core.common.exception.custom.BusinessException;

public class AdministrativeActionApiException extends BusinessException {

    public AdministrativeActionApiException(String message) {
        super(message);
    }

    public AdministrativeActionApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
