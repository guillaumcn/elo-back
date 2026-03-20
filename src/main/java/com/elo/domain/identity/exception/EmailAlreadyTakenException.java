package com.elo.domain.identity.exception;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Email already taken: " + email);
    }
}
