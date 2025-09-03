package com.kyut.ordo.feature.card.exception;

public class InsufficientCardPermissionsException extends RuntimeException {
    public InsufficientCardPermissionsException(String message) {
        super(message);
    }
}
