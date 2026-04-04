package com.elo.application.group.dto;

import java.time.Instant;
import java.util.UUID;

public record GroupInvitationResponse(
        UUID id,
        UUID groupId,
        String token,
        UUID invitedBy,
        Instant createdAt,
        Instant expiresAt
) {
}
