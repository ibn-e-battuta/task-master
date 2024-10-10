package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends TaskMasterException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
