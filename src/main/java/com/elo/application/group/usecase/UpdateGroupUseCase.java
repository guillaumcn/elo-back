package com.elo.application.group.usecase;

import com.elo.application.group.command.UpdateGroupCommand;
import com.elo.application.group.port.in.UpdateGroupPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateGroupUseCase implements UpdateGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public Group execute(UpdateGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsAdmin(group, command.requesterId());
        group.update(command.name(), command.description(), command.joinPolicy());
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
            throw new GroupAccessDeniedException("Only group admins can update group details");
        }
    }
}
