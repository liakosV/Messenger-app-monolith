package com.project.messenger.core.exception;

public class AppObjectUnauthorizedException extends AppGenericException {
    private final static String DEFAULT_CODE = "_NOT_AUTHORIZED";
    public AppObjectUnauthorizedException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
