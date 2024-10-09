package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotVerifiedException extends TaskMasterException {
    public UserNotVerifiedException(final String email) {
        super(email + " is not verified. Please verify your email before logging in.");
    }
}
