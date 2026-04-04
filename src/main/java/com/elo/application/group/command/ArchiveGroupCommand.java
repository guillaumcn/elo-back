package com.elo.application.group.command;

import java.util.UUID;

public record ArchiveGroupCommand(UUID groupId, UUID requesterId) {
}
