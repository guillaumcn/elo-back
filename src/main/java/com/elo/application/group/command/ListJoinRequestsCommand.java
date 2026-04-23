package com.elo.application.group.command;

import java.util.UUID;

public record ListJoinRequestsCommand(UUID groupId, UUID userId, int page, int size) {}
