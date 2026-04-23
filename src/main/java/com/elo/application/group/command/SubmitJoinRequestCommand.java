package com.elo.application.group.command;

import java.util.UUID;

public record SubmitJoinRequestCommand(UUID groupId, UUID userId) {}
