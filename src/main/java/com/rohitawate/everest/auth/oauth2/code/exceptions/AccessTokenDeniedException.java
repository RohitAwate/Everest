package com.rohitawate.everest.auth.oauth2.code.exceptions;

public class AccessTokenDeniedException extends Exception {
    public AccessTokenDeniedException(String message) {
        super(message);
    }
}
