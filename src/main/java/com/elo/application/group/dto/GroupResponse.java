package com.elo.application.group.dto;

import com.elo.domain.group.model.JoinPolicy;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        String description,
        JoinPolicy joinPolicy,
        boolean archived,
        UUID createdBy,
        Instant createdAt,
        int memberCount
) {
}
