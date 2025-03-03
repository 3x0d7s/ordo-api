package com.kyut.ordo.task.exception;

public class InsufficientTaskPermissionsException extends RuntimeException {
    public InsufficientTaskPermissionsException(String message) {
        super(message);
    }
}
