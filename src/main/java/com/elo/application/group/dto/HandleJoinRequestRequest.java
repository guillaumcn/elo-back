package com.elo.application.group.dto;

import com.elo.application.group.command.HandleJoinRequestCommand;
import com.elo.domain.group.model.JoinRequestAction;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HandleJoinRequestRequest(
        @NotNull JoinRequestAction action
) {
    public HandleJoinRequestCommand toCommand(UUID groupId, UUID requestId, UUID adminId) {
        return new HandleJoinRequestCommand(groupId, requestId, adminId, action);
    }
}
