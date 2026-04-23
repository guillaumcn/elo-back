package com.elo.domain.group.exception;

public class GroupJoinRequestDuplicateException extends RuntimeException {
    public GroupJoinRequestDuplicateException() {
        super("A pending join request already exists for this group");
    }
}
