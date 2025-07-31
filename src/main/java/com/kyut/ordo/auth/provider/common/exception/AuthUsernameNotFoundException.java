package com.kyut.ordo.auth.provider.common.exception;

public class AuthUsernameNotFoundException extends RuntimeException {
    public AuthUsernameNotFoundException(String message) {
        super(message);
    }

    public AuthUsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
