package com.elo.application.identity.command;

import java.util.UUID;

public record GetPublicProfileCommand(UUID requesterId, String targetUsername) {
}
