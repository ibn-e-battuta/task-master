package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SamePasswordException extends TaskMasterException {
    public SamePasswordException() {
        super("New password is the same as the old password.");
    }
}
