package com.elo.domain.group.exception;

public class GroupJoinPolicyViolationException extends RuntimeException {

    public GroupJoinPolicyViolationException() {
        super("This group does not allow direct joining");
    }
}
