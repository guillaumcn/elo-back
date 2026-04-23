package com.elo.domain.group.model;

import com.elo.domain.group.exception.GroupJoinRequestAlreadyResolvedException;
import com.elo.domain.group.exception.InvalidGroupException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class GroupJoinRequest {

    private UUID id;
    private UUID groupId;
    private UUID userId;
    private GroupJoinRequestStatus status;
    private Instant requestedAt;
    private UUID resolvedBy;
    private Instant resolvedAt;

    @Builder
    public GroupJoinRequest(UUID id, UUID groupId, UUID userId, GroupJoinRequestStatus status,
                            Instant requestedAt, UUID resolvedBy, Instant resolvedAt) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }

    public static GroupJoinRequest create(UUID groupId, UUID userId) {
        if (groupId == null) {
            throw new InvalidGroupException("Group ID is required");
        }
        if (userId == null) {
            throw new InvalidGroupException("User ID is required");
        }
        return GroupJoinRequest.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .userId(userId)
                .status(GroupJoinRequestStatus.PENDING)
                .requestedAt(Instant.now())
                .build();
    }

    public void approve(UUID adminId) {
        ensureNotAlreadyResolved();
        this.status = GroupJoinRequestStatus.APPROVED;
        this.resolvedBy = adminId;
        this.resolvedAt = Instant.now();
    }

    public void deny(UUID adminId) {
        ensureNotAlreadyResolved();
        this.status = GroupJoinRequestStatus.DENIED;
        this.resolvedBy = adminId;
        this.resolvedAt = Instant.now();
    }

    private void ensureNotAlreadyResolved() {
        if (this.status != GroupJoinRequestStatus.PENDING) {
            throw new GroupJoinRequestAlreadyResolvedException();
        }
    }
}
