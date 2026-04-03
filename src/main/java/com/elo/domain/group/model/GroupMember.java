package com.elo.domain.group.model;

import com.elo.domain.group.exception.InvalidGroupException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class GroupMember {

    private UUID id;
    private UUID groupId;
    private UUID userId;
    private MemberRole role;
    private Instant joinedAt;

    @Builder
    public GroupMember(UUID id, UUID groupId, UUID userId, MemberRole role, Instant joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static GroupMember createAdmin(UUID groupId, UUID userId) {
        if (groupId == null) {
            throw new InvalidGroupException("Group ID is required");
        }
        if (userId == null) {
            throw new InvalidGroupException("User ID is required");
        }

        return GroupMember.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .userId(userId)
                .role(MemberRole.ADMIN)
                .joinedAt(Instant.now())
                .build();
    }
}
