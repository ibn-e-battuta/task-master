package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class TokenExpiredException extends TaskMasterException {
    public TokenExpiredException(String token) {
        super("Verification token "+ token +" has expired.");
    }
}
