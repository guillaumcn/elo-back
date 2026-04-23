package com.elo.application.group.usecase;

import com.elo.application.group.command.SubmitJoinRequestCommand;
import com.elo.application.group.port.in.SubmitJoinRequestPort;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupJoinPolicyViolationException;
import com.elo.domain.group.exception.GroupJoinRequestDuplicateException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.JoinPolicy;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class SubmitJoinRequestUseCase implements SubmitJoinRequestPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupJoinRequestRepositoryPort joinRequestRepositoryPort;

    @Override
    public GroupJoinRequest execute(SubmitJoinRequestCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureGroupIsNotArchived(group);
        ensureGroupRequiresJoinRequest(group);
        ensureUserIsNotAlreadyMember(group, command.userId());
        ensureNoPendingRequestExists(command.groupId(), command.userId());
        GroupJoinRequest joinRequest = GroupJoinRequest.create(command.groupId(), command.userId());
        return joinRequestRepositoryPort.save(joinRequest);
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

    private void ensureGroupRequiresJoinRequest(Group group) {
        if (group.getJoinPolicy() != JoinPolicy.REQUEST) {
            throw new GroupJoinPolicyViolationException();
        }
    }

    private void ensureUserIsNotAlreadyMember(Group group, UUID userId) {
        if (group.hasMember(userId)) {
            throw new GroupAlreadyMemberException();
        }
    }

    private void ensureNoPendingRequestExists(UUID groupId, UUID userId) {
        joinRequestRepositoryPort.findPendingByGroupIdAndUserId(groupId, userId)
                .ifPresent(r -> { throw new GroupJoinRequestDuplicateException(); });
    }
}
