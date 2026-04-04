package com.elo.domain.group.exception;

public class GroupInvitationExpiredException extends RuntimeException {

    public GroupInvitationExpiredException() {
        super("Group invitation has expired");
    }
}
