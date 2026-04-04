package com.elo.application.group.dto;

import com.elo.application.group.command.JoinGroupByInvitationCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record JoinByInvitationRequest(
        @NotBlank(message = "Token is required") String token
) {

    public JoinGroupByInvitationCommand toCommand(UUID groupId, UUID userId) {
        return new JoinGroupByInvitationCommand(groupId, token, userId);
    }
}
