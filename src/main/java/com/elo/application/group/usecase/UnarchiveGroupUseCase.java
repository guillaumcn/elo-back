package com.elo.application.group.usecase;

import com.elo.application.group.command.UnarchiveGroupCommand;
import com.elo.application.group.port.in.UnarchiveGroupPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UnarchiveGroupUseCase implements UnarchiveGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public Group execute(UnarchiveGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsAdmin(group, command.requesterId());
        group.unarchive();
        return groupRepositoryPort.save(group);
    }

    private Group findGroupOrThrowNotFound(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureRequesterIsAdmin(Group group, UUID requesterId) {
        if (!group.hasAdmin(requesterId)) {
            if (!group.hasMember(requesterId)) {
                throw new GroupNotFoundException(group.getId());
            }
            throw new GroupAccessDeniedException("Only group admins can unarchive the group");
        }
    }
}
