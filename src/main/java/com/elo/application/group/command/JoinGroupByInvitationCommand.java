package com.elo.application.group.command;

import java.util.UUID;

public record JoinGroupByInvitationCommand(UUID groupId, String token, UUID userId) {
}
