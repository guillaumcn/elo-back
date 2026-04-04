package com.elo.application.group.command;

import java.time.Instant;
import java.util.UUID;

public record CreateGroupInvitationCommand(UUID groupId, UUID invitedBy, Instant expiresAt) {
}
