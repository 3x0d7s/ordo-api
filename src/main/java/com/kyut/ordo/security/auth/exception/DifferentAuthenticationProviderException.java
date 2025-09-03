package com.kyut.ordo.security.auth.exception;

public class DifferentAuthenticationProviderException extends RuntimeException {
    public DifferentAuthenticationProviderException(String message) {
        super(message);
    }

    public DifferentAuthenticationProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
