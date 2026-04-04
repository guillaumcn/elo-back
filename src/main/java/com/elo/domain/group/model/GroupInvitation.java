package com.elo.domain.group.model;

import com.elo.domain.group.exception.InvalidGroupException;
import lombok.Builder;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Getter
public class GroupInvitation {

    private UUID id;
    private UUID groupId;
    private String token;
    private UUID invitedBy;
    private Instant createdAt;
    private Instant expiresAt;

    @Builder
    public GroupInvitation(UUID id, UUID groupId, String token, UUID invitedBy, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.groupId = groupId;
        this.token = token;
        this.invitedBy = invitedBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static GroupInvitation create(UUID groupId, UUID invitedBy, Instant expiresAt) {
        if (groupId == null) {
            throw new InvalidGroupException("Group ID is required");
        }
        if (invitedBy == null) {
            throw new InvalidGroupException("Invited by is required");
        }

        return GroupInvitation.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .token(generateSecureToken())
                .invitedBy(invitedBy)
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .build();
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
