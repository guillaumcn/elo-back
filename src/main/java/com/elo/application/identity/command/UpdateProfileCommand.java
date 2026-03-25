package com.elo.application.identity.command;

import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String username, String avatarUrl, String bio) {
}
