package com.kyut.ordo.card.exception;

public class InsufficientCardPermissionsException extends RuntimeException {
    public InsufficientCardPermissionsException(String message) {
        super(message);
    }
}
