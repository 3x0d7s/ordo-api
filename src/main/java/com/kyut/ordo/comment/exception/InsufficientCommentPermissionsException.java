package com.kyut.ordo.comment.exception;

public class InsufficientCommentPermissionsException extends RuntimeException {
    public InsufficientCommentPermissionsException(String message) {
        super(message);
    }
}
