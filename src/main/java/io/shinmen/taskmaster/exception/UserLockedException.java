package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)
public class UserLockedException extends TaskMasterException {
    public UserLockedException(final String email) {
        super(email + " has been locked due to multiple failed login attempts.");
    }
}
