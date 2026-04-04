package com.elo.application.group.dto;

import java.time.Instant;
import java.util.UUID;

public record GroupMemberResponse(
        UUID id,
        UUID groupId,
        UUID userId,
        String role,
        Instant joinedAt
) {
}
