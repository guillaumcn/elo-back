package com.elo.application.group.usecase;

import com.elo.application.group.command.HandleJoinRequestCommand;
import com.elo.application.group.port.in.HandleJoinRequestPort;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupJoinRequestMismatchException;
import com.elo.domain.group.exception.GroupJoinRequestNotFoundException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.JoinRequestAction;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class HandleJoinRequestUseCase implements HandleJoinRequestPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupJoinRequestRepositoryPort joinRequestRepositoryPort;

    @Override
    @Transactional
    public GroupJoinRequest execute(HandleJoinRequestCommand command) {
        Group group = findGroupOrThrowNotFound(command.groupId());
        ensureUserIsAdmin(group, command.adminId());
        GroupJoinRequest joinRequest = findJoinRequestForGroup(command.requestId(), command.groupId());
        applyAction(joinRequest, command.action(), command.adminId());
        if (command.action() == JoinRequestAction.APPROVE) {
            addRequesterAsMember(group, joinRequest.getUserId());
            groupRepositoryPort.save(group);
        }
        return joinRequestRepositoryPort.save(joinRequest);
    }

    private Group findGroupOrThrowNotFound(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void ensureUserIsAdmin(Group group, UUID userId) {
        if (!group.hasAdmin(userId)) {
            throw new GroupAccessDeniedException("Only admins can handle join requests");
        }
    }

    private GroupJoinRequest findJoinRequestForGroup(UUID requestId, UUID groupId) {
        GroupJoinRequest joinRequest = joinRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new GroupJoinRequestNotFoundException(requestId));
        if (!joinRequest.getGroupId().equals(groupId)) {
            throw new GroupJoinRequestMismatchException();
        }
        return joinRequest;
    }

    private void applyAction(GroupJoinRequest joinRequest, JoinRequestAction action, UUID adminId) {
        switch (action) {
            case APPROVE -> joinRequest.approve(adminId);
            case DENY -> joinRequest.deny(adminId);
        }
    }

    private void addRequesterAsMember(Group group, UUID userId) {
        GroupMember newMember = GroupMember.createMember(group.getId(), userId);
        group.addMember(newMember);
    }
}
