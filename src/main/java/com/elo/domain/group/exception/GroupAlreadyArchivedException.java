package com.elo.domain.group.exception;

public class GroupAlreadyArchivedException extends RuntimeException {

    public GroupAlreadyArchivedException() {
        super("Group is already archived");
    }
}
