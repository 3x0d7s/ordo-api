package com.kyut.ordo.task.controller;

import com.kyut.ordo.common.dto.ErrorResponse;
import com.kyut.ordo.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class TaskControllerAdvice {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFoundException(TaskNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientTaskPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientCardPermissionsException(InsufficientTaskPermissionsException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
