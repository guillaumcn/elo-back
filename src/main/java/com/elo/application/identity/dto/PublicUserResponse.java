package com.elo.application.identity.dto;

import java.util.UUID;

public record PublicUserResponse(
        UUID id,
        String username,
        String avatarUrl,
        String bio
) {
}
