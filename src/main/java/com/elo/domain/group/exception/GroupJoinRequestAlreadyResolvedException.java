package com.elo.domain.group.exception;

public class GroupJoinRequestAlreadyResolvedException extends RuntimeException {
    public GroupJoinRequestAlreadyResolvedException() {
        super("This join request has already been resolved");
    }
}
