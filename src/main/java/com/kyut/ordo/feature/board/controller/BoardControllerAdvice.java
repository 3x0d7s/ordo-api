package com.kyut.ordo.feature.board.controller;

import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.common.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class BoardControllerAdvice {

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceNotFoundException(BoardNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientBoardPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBoardPermissionsException(InsufficientBoardPermissionsException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

}
