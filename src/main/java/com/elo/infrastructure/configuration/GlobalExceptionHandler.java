package com.elo.infrastructure.configuration;

import com.elo.domain.identity.exception.EmailAlreadyTakenException;
import com.elo.domain.identity.exception.InvalidCredentialsException;
import com.elo.domain.identity.exception.InvalidUserException;
import com.elo.domain.identity.exception.UserNotFoundException;
import com.elo.domain.identity.exception.UsernameAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return ErrorResponse.of(400, "BAD_REQUEST", message);
    }

    @ExceptionHandler(InvalidUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidUser(InvalidUserException ex) {
        return ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUsernameAlreadyTaken(UsernameAlreadyTakenException ex) {
        return ErrorResponse.of(409, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyTaken(EmailAlreadyTakenException ex) {
        return ErrorResponse.of(409, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return ErrorResponse.of(401, "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return ErrorResponse.of(404, "NOT_FOUND", ex.getMessage());
    }
}
