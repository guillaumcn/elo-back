package com.elo.application.group.port.out;

import com.elo.domain.group.model.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepositoryPort {

    Group save(Group group);

    Optional<Group> findById(UUID groupId);

    List<Group> findAllByMemberId(UUID userId);
}
