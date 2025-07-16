package com.kyut.ordo.auth.provider.exception;

public class AuthUsernameNotFoundException extends RuntimeException {
    public AuthUsernameNotFoundException(String message) {
        super(message);
    }

    public AuthUsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
