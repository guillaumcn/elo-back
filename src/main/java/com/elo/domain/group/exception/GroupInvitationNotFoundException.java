package com.elo.domain.group.exception;

public class GroupInvitationNotFoundException extends RuntimeException {

    public GroupInvitationNotFoundException() {
        super("Group invitation not found");
    }
}
