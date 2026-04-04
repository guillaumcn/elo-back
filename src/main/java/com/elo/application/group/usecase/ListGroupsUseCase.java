package com.elo.application.group.usecase;

import com.elo.application.group.port.in.ListGroupsPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ListGroupsUseCase implements ListGroupsPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public PagedResult<Group> execute(UUID userId, int page, int size) {
        return groupRepositoryPort.findAllByMemberId(userId, page, size);
    }
}
