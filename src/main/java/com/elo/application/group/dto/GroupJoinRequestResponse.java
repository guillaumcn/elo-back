package com.elo.application.group.dto;

import com.elo.domain.group.model.GroupJoinRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record GroupJoinRequestResponse(
        UUID id,
        UUID groupId,
        UUID userId,
        GroupJoinRequestStatus status,
        Instant requestedAt,
        UUID resolvedBy,
        Instant resolvedAt
) {}
