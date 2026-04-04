package com.elo.application.group.command;

import java.util.UUID;

public record JoinGroupCommand(UUID groupId, UUID userId) {
}
