package com.litmus.authPortal.exceptions.advice;

import java.util.HashMap;
import java.util.Map;

import com.litmus.authPortal.dto.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GenericResponse<Void>> userAlreadyExists(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GenericResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GenericResponse<Void>> badCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse<>(false, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

}
