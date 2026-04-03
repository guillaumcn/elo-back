package com.elo.application.group.usecase;

import com.elo.application.group.command.GetGroupCommand;
import com.elo.application.group.port.in.GetGroupPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetGroupUseCase implements GetGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupMemberRepositoryPort groupMemberRepositoryPort;

    @Override
    public Group execute(GetGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsMember(command);
        return group;
    }

    private Group findGroupOrThrowNotFound(java.util.UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureRequesterIsMember(GetGroupCommand command) {
        if (!groupMemberRepositoryPort.existsByGroupIdAndUserId(command.groupId(), command.requesterId())) {
            throw new GroupNotFoundException(command.groupId());
        }
    }
}
