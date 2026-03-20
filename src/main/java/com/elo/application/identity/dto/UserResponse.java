package com.elo.application.identity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String avatarUrl,
        String bio,
        Instant createdAt
) {
}
