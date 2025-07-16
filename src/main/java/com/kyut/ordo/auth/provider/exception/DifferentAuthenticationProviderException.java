package com.kyut.ordo.auth.provider.exception;

public class DifferentAuthenticationProviderException extends RuntimeException {
    public DifferentAuthenticationProviderException(String message) {
        super(message);
    }

    public DifferentAuthenticationProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
