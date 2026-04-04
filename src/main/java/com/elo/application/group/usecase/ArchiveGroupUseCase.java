package com.elo.application.group.usecase;

import com.elo.application.group.command.ArchiveGroupCommand;
import com.elo.application.group.port.in.ArchiveGroupPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ArchiveGroupUseCase implements ArchiveGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public Group execute(ArchiveGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsAdmin(group, command.requesterId());
        group.archive();
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
            throw new GroupAccessDeniedException("Only group admins can archive the group");
        }
    }
}
