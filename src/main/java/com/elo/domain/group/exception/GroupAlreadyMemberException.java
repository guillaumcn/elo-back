package com.elo.domain.group.exception;

public class GroupAlreadyMemberException extends RuntimeException {

    public GroupAlreadyMemberException() {
        super("User is already a member of this group");
    }
}
