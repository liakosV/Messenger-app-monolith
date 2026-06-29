package com.project.messenger.core.exception;

public class AppObjectAccessDeniedException extends AppGenericException {
    private final static String DEFAULT_CODE = "_ACCESS_DENIED";
    public AppObjectAccessDeniedException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
