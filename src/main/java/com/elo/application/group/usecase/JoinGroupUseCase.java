package com.elo.application.group.usecase;

import com.elo.application.group.command.JoinGroupCommand;
import com.elo.application.group.port.in.JoinGroupPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupJoinPolicyViolationException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.JoinPolicy;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class JoinGroupUseCase implements JoinGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupMemberRepositoryPort groupMemberRepositoryPort;

    @Override
    public GroupMember execute(JoinGroupCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureGroupIsNotArchived(group);
        ensureGroupIsOpen(group);
        ensureUserIsNotAlreadyMember(command.groupId(), command.userId());
        return groupMemberRepositoryPort.save(GroupMember.createMember(command.groupId(), command.userId()));
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

    private void ensureGroupIsOpen(Group group) {
        if (group.getJoinPolicy() != JoinPolicy.OPEN) {
            throw new GroupJoinPolicyViolationException();
        }
    }

    private void ensureUserIsNotAlreadyMember(UUID groupId, UUID userId) {
        if (groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GroupAlreadyMemberException();
        }
    }
}
