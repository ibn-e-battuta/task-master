package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VerificationTokenNotFoundException extends TaskMasterException {
    public VerificationTokenNotFoundException(String verificationToken) {
        super("Verification token " + verificationToken + " not found.");
    }
}
