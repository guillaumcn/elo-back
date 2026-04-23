package com.elo.application.group.port.out;

import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.GroupJoinRequest;

import java.util.Optional;
import java.util.UUID;

public interface GroupJoinRequestRepositoryPort {
    GroupJoinRequest save(GroupJoinRequest joinRequest);
    Optional<GroupJoinRequest> findById(UUID id);
    Optional<GroupJoinRequest> findPendingByGroupIdAndUserId(UUID groupId, UUID userId);
    PagedResult<GroupJoinRequest> findAllByGroupId(UUID groupId, int page, int size);
}
