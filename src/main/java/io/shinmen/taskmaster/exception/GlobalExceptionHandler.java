package io.shinmen.taskmaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import io.shinmen.taskmaster.dto.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        ApiResponse response = ApiResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Add more specific exception handlers if needed
}
