package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupInvitationCommand;
import com.elo.application.group.port.in.CreateGroupInvitationPort;
import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupInvitation;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateGroupInvitationUseCase implements CreateGroupInvitationPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupInvitationRepositoryPort groupInvitationRepositoryPort;

    @Override
    public GroupInvitation execute(CreateGroupInvitationCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsMember(group, command.invitedBy());
        GroupInvitation invitation = GroupInvitation.create(command.groupId(), command.invitedBy(), command.expiresAt());
        return groupInvitationRepositoryPort.save(invitation);
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
