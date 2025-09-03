package com.kyut.ordo.feature.list.controller;

import com.kyut.ordo.common.dto.ErrorResponse;
import com.kyut.ordo.feature.list.exception.ListNotFoundException;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class ListControllerAdvice {

    @ExceptionHandler(ListNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFoundException(ListNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

}
