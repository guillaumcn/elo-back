package com.elo.application.group.usecase;

import com.elo.application.group.port.in.ListGroupsPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ListGroupsUseCase implements ListGroupsPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public List<Group> execute(UUID userId) {
        return groupRepositoryPort.findAllByMemberId(userId);
    }
}
