package com.elo.application.group.usecase;

import com.elo.application.group.command.UpdateGroupCommand;
import com.elo.application.group.port.in.UpdateGroupPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.MemberRole;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateGroupUseCase implements UpdateGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupMemberRepositoryPort groupMemberRepositoryPort;

    @Override
    public Group execute(UpdateGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsAdmin(command);
        group.update(command.name(), command.description(), command.joinPolicy());
        return groupRepositoryPort.save(group);
    }

    private Group findGroupOrThrowNotFound(java.util.UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureRequesterIsAdmin(UpdateGroupCommand command) {
        if (!groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(
                command.groupId(), command.requesterId(), MemberRole.ADMIN)) {
            if (!groupMemberRepositoryPort.existsByGroupIdAndUserId(command.groupId(), command.requesterId())) {
                throw new GroupNotFoundException(command.groupId());
            }
            throw new GroupAccessDeniedException("Only group admins can update group details");
        }
    }
}
