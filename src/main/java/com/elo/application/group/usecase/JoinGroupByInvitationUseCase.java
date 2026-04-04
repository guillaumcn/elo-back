package com.elo.application.group.usecase;

import com.elo.application.group.command.JoinGroupByInvitationCommand;
import com.elo.application.group.port.in.JoinGroupByInvitationPort;
import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupInvitationExpiredException;
import com.elo.domain.group.exception.GroupInvitationNotFoundException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupInvitation;
import com.elo.domain.group.model.GroupMember;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class JoinGroupByInvitationUseCase implements JoinGroupByInvitationPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupMemberRepositoryPort groupMemberRepositoryPort;
    private final GroupInvitationRepositoryPort groupInvitationRepositoryPort;

    @Override
    public GroupMember execute(JoinGroupByInvitationCommand command) {
        GroupInvitation invitation = findInvitationOrThrowNotFound(command.token(), command.groupId());
        ensureInvitationIsNotExpired(invitation);
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureGroupIsNotArchived(group);
        ensureUserIsNotAlreadyMember(command.groupId(), command.userId());
        return groupMemberRepositoryPort.save(GroupMember.createMember(command.groupId(), command.userId()));
    }

    private GroupInvitation findInvitationOrThrowNotFound(String token, UUID groupId) {
        GroupInvitation invitation = groupInvitationRepositoryPort.findByToken(token)
                .orElseThrow(GroupInvitationNotFoundException::new);
        if (!invitation.getGroupId().equals(groupId)) {
            throw new GroupInvitationNotFoundException();
        }
        return invitation;
    }

    private void ensureInvitationIsNotExpired(GroupInvitation invitation) {
        if (invitation.isExpired()) {
            throw new GroupInvitationExpiredException();
        }
    }

    private Group findGroupOrThrowNotFound(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureGroupIsNotArchived(Group group) {
        if (group.isArchived()) {
            throw new GroupAlreadyArchivedException();
        }
    }

    private void ensureUserIsNotAlreadyMember(UUID groupId, UUID userId) {
        if (groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GroupAlreadyMemberException();
        }
    }
}
