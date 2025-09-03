package com.kyut.ordo.feature.workspace.exception;

public class WorkspaceRoleInsuficientRightsExceptions extends Exception {

    public WorkspaceRoleInsuficientRightsExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceRoleInsuficientRightsExceptions(String message) {
        super(message);
    }
}
