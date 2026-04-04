package com.elo.application.group.command;

import java.util.UUID;

public record UnarchiveGroupCommand(UUID groupId, UUID requesterId) {
}
