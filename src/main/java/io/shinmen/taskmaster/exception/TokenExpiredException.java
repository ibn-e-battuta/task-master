package io.shinmen.taskmaster.exception;

public class TokenExpiredException extends TaskMasterException {
    public TokenExpiredException(String token) {
        super("Verification token "+ token +" has expired.");
    }
}
