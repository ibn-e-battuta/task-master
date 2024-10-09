package io.shinmen.taskmaster.exception;

public class TaskMasterException extends RuntimeException {
    public TaskMasterException(final String message) {
        super(message);
    }

    public TaskMasterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
