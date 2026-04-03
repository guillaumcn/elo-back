package com.elo.application.group.dto;

import com.elo.application.group.command.UpdateGroupCommand;
import com.elo.domain.group.model.JoinPolicy;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateGroupRequest(
        @Size(max = 100, message = "Group name must be at most 100 characters")
        String name,

        String description,

        JoinPolicy joinPolicy
) {
    public UpdateGroupCommand toCommand(UUID groupId, UUID requesterId) {
        return new UpdateGroupCommand(groupId, requesterId, name, description, joinPolicy);
    }
}
