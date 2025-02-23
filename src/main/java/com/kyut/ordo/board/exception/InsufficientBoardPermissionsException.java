package com.kyut.ordo.board.exception;

public class InsufficientBoardPermissionsException extends Exception{
    public InsufficientBoardPermissionsException(String message) {
        super(message);
    }

    public InsufficientBoardPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
