package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends TaskMasterException {
    public UserAlreadyExistsException(String user) {
        super(user + " is already in use.");
    }
}
