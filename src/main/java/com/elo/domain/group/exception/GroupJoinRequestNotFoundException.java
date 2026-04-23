package com.elo.domain.group.exception;

import java.util.UUID;

public class GroupJoinRequestNotFoundException extends RuntimeException {
    public GroupJoinRequestNotFoundException(UUID requestId) {
        super("Join request not found: " + requestId);
    }
}
