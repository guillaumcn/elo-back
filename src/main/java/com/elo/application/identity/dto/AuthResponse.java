package com.elo.application.identity.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
