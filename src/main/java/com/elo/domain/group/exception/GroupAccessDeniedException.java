package com.elo.domain.group.exception;

public class GroupAccessDeniedException extends RuntimeException {

    public GroupAccessDeniedException(String message) {
        super(message);
    }
}
