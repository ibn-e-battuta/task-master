package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenExpiredException extends TaskMasterException {
    public TokenExpiredException(String token, String tokenName) {
        super(tokenName + " " + token +" has expired.");
    }
}
