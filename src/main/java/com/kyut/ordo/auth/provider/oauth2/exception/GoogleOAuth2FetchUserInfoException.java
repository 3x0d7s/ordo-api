package com.kyut.ordo.auth.provider.oauth2.exception;

public class GoogleOAuth2FetchUserInfoException extends RuntimeException {
    public GoogleOAuth2FetchUserInfoException(String message) {
        super(message);
    }

    public GoogleOAuth2FetchUserInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
