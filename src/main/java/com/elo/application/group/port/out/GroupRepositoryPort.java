package com.elo.application.group.port.out;

import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.Group;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepositoryPort {

    Group save(Group group);

    Optional<Group> findById(UUID groupId);

    PagedResult<Group> findAllByMemberId(UUID userId, int page, int size);
}
