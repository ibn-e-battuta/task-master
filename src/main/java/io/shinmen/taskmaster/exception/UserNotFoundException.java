package io.shinmen.taskmaster.exception;

public class UserNotFoundException extends TaskMasterException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
