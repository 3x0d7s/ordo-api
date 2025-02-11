package com.kyut.ordo.auth.oauth2.controller;

import com.kyut.ordo.auth.oauth2.exception.GoogleOAuth2CodeForTokenExchangeException;
import com.kyut.ordo.auth.oauth2.exception.OAuth2AuthenticationException;
import com.kyut.ordo.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class OAuth2ControllerAdvice {

    @ExceptionHandler(GoogleOAuth2CodeForTokenExchangeException.class)
    public ResponseEntity<ErrorResponse> handleGoogleOAuth2CodeForTokenExchangeException(GoogleOAuth2CodeForTokenExchangeException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleGoogleOAuth2AuthenticationException(OAuth2AuthenticationException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

}
