package com.elo.application.group.command;

import java.util.UUID;

public record GetGroupCommand(UUID groupId, UUID requesterId) {
}
