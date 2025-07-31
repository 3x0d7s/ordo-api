package com.kyut.ordo.auth.provider.oauth2.exception;

public class GoogleOAuth2CodeForTokenExchangeException extends RuntimeException {
    public GoogleOAuth2CodeForTokenExchangeException(String message) {
        super(message);
    }

    public GoogleOAuth2CodeForTokenExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
