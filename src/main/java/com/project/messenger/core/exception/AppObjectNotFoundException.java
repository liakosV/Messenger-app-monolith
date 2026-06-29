package com.project.messenger.core.exception;

public class AppObjectNotFoundException extends AppGenericException {
    private final static String DEFAULT_CODE = "_NOT_FOUND";
    public AppObjectNotFoundException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
