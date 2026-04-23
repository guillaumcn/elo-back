package com.elo.application.group.command;

import com.elo.domain.group.model.JoinRequestAction;

import java.util.UUID;

public record HandleJoinRequestCommand(UUID groupId, UUID requestId, UUID adminId, JoinRequestAction action) {}
