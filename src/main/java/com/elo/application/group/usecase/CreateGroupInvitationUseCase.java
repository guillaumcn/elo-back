package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupInvitationCommand;
import com.elo.application.group.port.in.CreateGroupInvitationPort;
import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupInvitation;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateGroupInvitationUseCase implements CreateGroupInvitationPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupMemberRepositoryPort groupMemberRepositoryPort;
    private final GroupInvitationRepositoryPort groupInvitationRepositoryPort;

    @Override
    public GroupInvitation execute(CreateGroupInvitationCommand command) {
        findGroupOrThrowNotFound(command.groupId());
        ensureRequesterIsMember(command.groupId(), command.invitedBy());
        GroupInvitation invitation = GroupInvitation.create(command.groupId(), command.invitedBy(), command.expiresAt());
        return groupInvitationRepositoryPort.save(invitation);
    }

    private Group findGroupOrThrowNotFound(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureRequesterIsMember(UUID groupId, UUID requesterId) {
        if (!groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new GroupNotFoundException(groupId);
        }
    }
}
