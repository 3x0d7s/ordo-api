package com.kyut.ordo.workspace.exception;

public class WorkspaceNotFoundException extends Exception {
    public WorkspaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceNotFoundException(String message) {
        super(message);
    }
}
