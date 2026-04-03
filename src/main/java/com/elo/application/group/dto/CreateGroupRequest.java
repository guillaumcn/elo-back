package com.elo.application.group.dto;

import com.elo.application.group.command.CreateGroupCommand;
import com.elo.domain.group.model.JoinPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateGroupRequest(
        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Group name must be at most 100 characters")
        String name,

        String description,

        @NotNull(message = "Join policy is required")
        JoinPolicy joinPolicy
) {
    public CreateGroupCommand toCommand(UUID creatorId) {
        return new CreateGroupCommand(creatorId, name, description, joinPolicy);
    }
}
