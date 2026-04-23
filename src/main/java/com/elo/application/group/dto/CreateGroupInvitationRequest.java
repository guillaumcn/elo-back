package com.elo.application.group.dto;

import com.elo.application.group.command.CreateGroupInvitationCommand;

import java.time.Instant;
import java.util.UUID;

public record CreateGroupInvitationRequest(Instant expiresAt) {

    public CreateGroupInvitationCommand toCommand(UUID groupId, UUID invitedBy) {
        return new CreateGroupInvitationCommand(groupId, invitedBy, expiresAt);
    }
}
