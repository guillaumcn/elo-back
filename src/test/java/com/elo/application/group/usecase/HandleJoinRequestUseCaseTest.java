package com.elo.application.group.usecase;

import com.elo.application.group.command.HandleJoinRequestCommand;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupJoinRequestAlreadyResolvedException;
import com.elo.domain.group.exception.GroupJoinRequestMismatchException;
import com.elo.domain.group.exception.GroupJoinRequestNotFoundException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.GroupJoinRequestStatus;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.JoinPolicy;
import com.elo.domain.group.model.JoinRequestAction;
import com.elo.domain.group.model.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleJoinRequestUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupJoinRequestRepositoryPort joinRequestRepositoryPort;

    private HandleJoinRequestUseCase handleJoinRequestUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID requesterId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handleJoinRequestUseCase = new HandleJoinRequestUseCase(groupRepositoryPort, joinRequestRepositoryPort);
    }

    @Test
    void shouldApproveRequestAndAddMember() {
        Group group = buildGroupWithAdmin();
        GroupJoinRequest pending = GroupJoinRequest.create(groupId, requesterId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(pending));
        when(joinRequestRepositoryPort.save(any(GroupJoinRequest.class))).thenAnswer(i -> i.getArgument(0));
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        GroupJoinRequest result = handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, adminId, JoinRequestAction.APPROVE));

        assertThat(result.getStatus()).isEqualTo(GroupJoinRequestStatus.APPROVED);
        assertThat(result.getResolvedBy()).isEqualTo(adminId);
        assertThat(group.hasMember(requesterId)).isTrue();
        verify(groupRepositoryPort).save(group);
        verify(joinRequestRepositoryPort).save(pending);
    }

    @Test
    void shouldDenyRequestWithoutAddingMember() {
        Group group = buildGroupWithAdmin();
        GroupJoinRequest pending = GroupJoinRequest.create(groupId, requesterId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(pending));
        when(joinRequestRepositoryPort.save(any(GroupJoinRequest.class))).thenAnswer(i -> i.getArgument(0));

        GroupJoinRequest result = handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, adminId, JoinRequestAction.DENY));

        assertThat(result.getStatus()).isEqualTo(GroupJoinRequestStatus.DENIED);
        assertThat(result.getResolvedBy()).isEqualTo(adminId);
        assertThat(group.hasMember(requesterId)).isFalse();
        verify(groupRepositoryPort, never()).save(any(Group.class));
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, adminId, JoinRequestAction.APPROVE)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowAccessDeniedWhenUserIsNotAdmin() {
        Group group = buildGroupWithAdmin();
        UUID nonAdminId = UUID.randomUUID();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, nonAdminId, JoinRequestAction.APPROVE)))
                .isInstanceOf(GroupAccessDeniedException.class);
    }

    @Test
    void shouldThrowNotFoundWhenRequestDoesNotExist() {
        Group group = buildGroupWithAdmin();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, adminId, JoinRequestAction.APPROVE)))
                .isInstanceOf(GroupJoinRequestNotFoundException.class);
    }

    @Test
    void shouldThrowMismatchWhenRequestBelongsToAnotherGroup() {
        Group group = buildGroupWithAdmin();
        UUID otherGroupId = UUID.randomUUID();
        GroupJoinRequest otherGroupRequest = GroupJoinRequest.create(otherGroupId, requesterId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(otherGroupRequest));

        assertThatThrownBy(() -> handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, adminId, JoinRequestAction.APPROVE)))
                .isInstanceOf(GroupJoinRequestMismatchException.class);
    }

    @Test
    void shouldThrowAlreadyResolvedWhenRequestIsAlreadyApproved() {
        Group group = buildGroupWithAdmin();
        GroupJoinRequest resolved = GroupJoinRequest.create(groupId, requesterId);
        resolved.approve(adminId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(resolved));

        assertThatThrownBy(() -> handleJoinRequestUseCase.execute(
                new HandleJoinRequestCommand(groupId, requestId, adminId, JoinRequestAction.APPROVE)))
                .isInstanceOf(GroupJoinRequestAlreadyResolvedException.class);
    }

    private Group buildGroupWithAdmin() {
        return Group.builder()
                .id(groupId)
                .name("Test Group")
                .joinPolicy(JoinPolicy.REQUEST)
                .archived(false)
                .createdBy(adminId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .members(List.of(GroupMember.builder()
                        .id(UUID.randomUUID())
                        .groupId(groupId)
                        .userId(adminId)
                        .role(MemberRole.ADMIN)
                        .joinedAt(Instant.now())
                        .build()))
                .build();
    }
}
