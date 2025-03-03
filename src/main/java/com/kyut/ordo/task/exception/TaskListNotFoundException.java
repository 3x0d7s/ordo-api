package com.kyut.ordo.task.exception;

public class TaskListNotFoundException extends RuntimeException {
    public TaskListNotFoundException(String message) {
        super(message);
    }
}
