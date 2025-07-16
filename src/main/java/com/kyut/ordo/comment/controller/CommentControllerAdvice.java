package com.kyut.ordo.comment.controller;

import com.kyut.ordo.comment.exception.CommentNotFoundException;
import com.kyut.ordo.comment.exception.InsufficientCommentPermissionsException;

import com.kyut.ordo.core.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class CommentControllerAdvice {

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFoundException(CommentNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientCommentPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientCardPermissionsException(InsufficientCommentPermissionsException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

}
