package com.elo.application.group.command;

import com.elo.domain.group.model.JoinPolicy;

import java.util.UUID;

public record UpdateGroupCommand(UUID groupId, UUID requesterId, String name, String description, JoinPolicy joinPolicy) {
}
