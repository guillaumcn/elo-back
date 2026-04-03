package com.elo.application.identity.dto;

import com.elo.application.identity.command.UpdateProfileCommand;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProfileRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Size(max = 500, message = "Avatar URL must be at most 500 characters")
        @Pattern(regexp = "^https://.*", message = "Avatar URL must use HTTPS")
        String avatarUrl,

        @Size(max = 500, message = "Bio must be at most 500 characters")
        String bio
) {
    public UpdateProfileCommand toCommand(UUID userId) {
        return new UpdateProfileCommand(userId, username, avatarUrl, bio);
    }
}
