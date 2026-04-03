package com.elo.domain.group.model;

import com.elo.domain.group.exception.InvalidGroupException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Group {

    private UUID id;
    private String name;
    private String description;
    private JoinPolicy joinPolicy;
    private boolean archived;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private int memberCount;

    private static final int NAME_MAX_LENGTH = 100;

    @Builder
    public Group(UUID id, String name, String description, JoinPolicy joinPolicy,
                 boolean archived, UUID createdBy, Instant createdAt, Instant updatedAt, int memberCount) {
        validateName(name);
        if (joinPolicy == null) {
            throw new InvalidGroupException("Join policy is required");
        }
        if (createdBy == null) {
            throw new InvalidGroupException("Creator is required");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.joinPolicy = joinPolicy;
        this.archived = archived;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.memberCount = memberCount;
    }

    public static Group create(String name, String description, JoinPolicy joinPolicy, UUID createdBy) {
        Instant now = Instant.now();
        return Group.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .joinPolicy(joinPolicy)
                .archived(false)
                .createdBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .memberCount(0)
                .build();
    }

    public void update(String name, String description, JoinPolicy joinPolicy) {
        if (name != null) {
            validateName(name);
            this.name = name;
        }
        if (description != null) this.description = description;
        if (joinPolicy != null) this.joinPolicy = joinPolicy;
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidGroupException("Group name is required");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new InvalidGroupException("Group name must be at most " + NAME_MAX_LENGTH + " characters");
        }
    }
}
