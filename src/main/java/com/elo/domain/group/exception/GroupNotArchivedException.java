package com.elo.domain.group.exception;

public class GroupNotArchivedException extends RuntimeException {

    public GroupNotArchivedException() {
        super("Group is not archived");
    }
}
