package com.kyut.ordo.workspace.controller;

import com.kyut.ordo.core.dto.ErrorResponse;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class WorkspaceControllerAdvice {

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceNotFoundException(WorkspaceNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(WorkspaceRoleInsuficientRightsExceptions.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceRoleInsufficientRightsExceptions(WorkspaceRoleInsuficientRightsExceptions e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

}
