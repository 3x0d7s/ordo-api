package com.kyut.ordo.feature.comment.exception;

public class InsufficientCommentPermissionsException extends RuntimeException {
    public InsufficientCommentPermissionsException(String message) {
        super(message);
    }
}
