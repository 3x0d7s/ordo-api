package com.kyut.ordo.security.auth.oauth2.exception;

public class OAuth2AuthenticationException extends RuntimeException {
    public OAuth2AuthenticationException(String message) {
        super(message);
    }

    public OAuth2AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
