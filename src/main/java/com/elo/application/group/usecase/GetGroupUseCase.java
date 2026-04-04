package com.elo.application.group.usecase;

import com.elo.application.group.command.GetGroupCommand;
import com.elo.application.group.port.in.GetGroupPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetGroupUseCase implements GetGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public Group execute(GetGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsMember(group, command.requesterId());
        return group;
    }

    private Group findGroupOrThrowNotFound(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureRequesterIsMember(Group group, UUID requesterId) {
        if (!group.hasMember(requesterId)) {
            throw new GroupNotFoundException(group.getId());
        }
    }
}
