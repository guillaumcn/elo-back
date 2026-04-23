package com.elo.domain.group.exception;

public class GroupJoinRequestMismatchException extends RuntimeException {
    public GroupJoinRequestMismatchException() {
        super("Join request does not belong to this group");
    }
}
