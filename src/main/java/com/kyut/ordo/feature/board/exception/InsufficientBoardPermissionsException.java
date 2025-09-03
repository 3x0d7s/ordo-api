package com.kyut.ordo.feature.board.exception;

public class InsufficientBoardPermissionsException extends Exception{
    public InsufficientBoardPermissionsException(String message) {
        super(message);
    }

    public InsufficientBoardPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
