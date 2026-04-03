package com.elo.application.group.command;

import com.elo.domain.group.model.JoinPolicy;

import java.util.UUID;

public record CreateGroupCommand(UUID creatorId, String name, String description, JoinPolicy joinPolicy) {
}
