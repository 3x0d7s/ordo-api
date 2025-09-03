package com.kyut.ordo.feature.task.exception;

public class InsufficientTaskPermissionsException extends RuntimeException {
    public InsufficientTaskPermissionsException(String message) {
        super(message);
    }
}
