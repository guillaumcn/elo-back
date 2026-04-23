package com.elo.application.group.usecase;

import com.elo.application.group.command.ListJoinRequestsCommand;
import com.elo.application.group.port.in.ListJoinRequestsPort;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupJoinRequest;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ListJoinRequestsUseCase implements ListJoinRequestsPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupJoinRequestRepositoryPort joinRequestRepositoryPort;

    @Override
    public PagedResult<GroupJoinRequest> execute(ListJoinRequestsCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureUserIsAdmin(group, command.userId());
        return joinRequestRepositoryPort.findAllByGroupId(command.groupId(), command.page(), command.size());
    }

    private Group findGroupOrThrowNotFound(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureUserIsAdmin(Group group, UUID userId) {
        if (!group.hasAdmin(userId)) {
            throw new GroupAccessDeniedException("Only admins can list join requests");
        }
    }
}
