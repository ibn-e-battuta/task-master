package io.shinmen.taskmaster.exception;

import java.time.ZonedDateTime;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import io.shinmen.taskmaster.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskMasterException.class)
    public ResponseEntity<ErrorResponse> handleTaskMasterException(final TaskMasterException ex) {

        log.error("Exception occurred: {}", ex.getMessage(), ex);

        HttpStatus status = extractResponseStatusFromAnnotation(ex);

        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }

        final String message = ex.getMessage();

        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .message("ERROR: " + message)
                .timestamp(ZonedDateTime.now())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
            final InternalAuthenticationServiceException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof TaskMasterException) {
            return handleTaskMasterException((TaskMasterException) cause);
        }

        log.error("InternalAuthenticationServiceException occurred: {}", ex.getMessage(), ex);

        final HttpStatus status = HttpStatus.UNAUTHORIZED;

        final String message = ex.getMessage();

        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .message("ERROR: " + message)
                .timestamp(ZonedDateTime.now())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    // Catch-all handler for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("An unexpected error occurred.")
                .timestamp(ZonedDateTime.now())
                .details(Collections.singletonList(ex.getMessage()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus extractResponseStatusFromAnnotation(final Exception ex) {
        final ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);

        if (responseStatus != null) {
            return responseStatus.value();
        }

        return null;
    }
}
