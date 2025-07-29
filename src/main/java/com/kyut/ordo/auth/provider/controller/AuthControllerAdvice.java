package com.kyut.ordo.auth.provider.controller;

import com.kyut.ordo.auth.provider.exception.AuthUsernameNotFoundException;
import com.kyut.ordo.auth.provider.exception.DifferentAuthenticationProviderException;
import com.kyut.ordo.core.common.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class AuthControllerAdvice {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AuthUsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthUsernameNotFoundException(AuthUsernameNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(DifferentAuthenticationProviderException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2DifferentAuthenticationProviderException(DifferentAuthenticationProviderException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

}
