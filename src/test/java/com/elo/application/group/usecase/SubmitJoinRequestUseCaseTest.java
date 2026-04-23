package com.elo.application.group.usecase;

import com.elo.application.group.command.SubmitJoinRequestCommand;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupJoinPolicyViolationException;
import com.elo.domain.group.exception.GroupJoinRequestDuplicateException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.JoinPolicy;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitJoinRequestUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupJoinRequestRepositoryPort joinRequestRepositoryPort;

    private SubmitJoinRequestUseCase submitJoinRequestUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        submitJoinRequestUseCase = new SubmitJoinRequestUseCase(groupRepositoryPort, joinRequestRepositoryPort);
    }

    @Test
    void shouldSubmitJoinRequestForRequestPolicyGroup() {
        Group group = buildGroup(JoinPolicy.REQUEST, false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findPendingByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());
        when(joinRequestRepositoryPort.save(any(GroupJoinRequest.class))).thenAnswer(i -> i.getArgument(0));

        GroupJoinRequest result = submitJoinRequestUseCase.execute(new SubmitJoinRequestCommand(groupId, userId));

        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(joinRequestRepositoryPort).save(any(GroupJoinRequest.class));
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submitJoinRequestUseCase.execute(new SubmitJoinRequestCommand(groupId, userId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowWhenGroupIsArchived() {
        Group group = buildGroup(JoinPolicy.REQUEST, true);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> submitJoinRequestUseCase.execute(new SubmitJoinRequestCommand(groupId, userId)))
                .isInstanceOf(GroupAlreadyArchivedException.class);
    }

    @Test
    void shouldThrowWhenGroupIsNotRequestPolicy() {
        Group group = buildGroup(JoinPolicy.OPEN, false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> submitJoinRequestUseCase.execute(new SubmitJoinRequestCommand(groupId, userId)))
                .isInstanceOf(GroupJoinPolicyViolationException.class);
    }

    @Test
    void shouldThrowWhenUserIsAlreadyMember() {
        Group group = buildGroupWithMember(userId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> submitJoinRequestUseCase.execute(new SubmitJoinRequestCommand(groupId, userId)))
                .isInstanceOf(GroupAlreadyMemberException.class);
    }

    @Test
    void shouldThrowWhenDuplicatePendingRequestExists() {
        Group group = buildGroup(JoinPolicy.REQUEST, false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findPendingByGroupIdAndUserId(groupId, userId))
                .thenReturn(Optional.of(GroupJoinRequest.create(groupId, userId)));

        assertThatThrownBy(() -> submitJoinRequestUseCase.execute(new SubmitJoinRequestCommand(groupId, userId)))
                .isInstanceOf(GroupJoinRequestDuplicateException.class);
    }

    private Group buildGroup(JoinPolicy joinPolicy, boolean archived) {
        return Group.builder()
                .id(groupId)
                .name("Test Group")
                .joinPolicy(joinPolicy)
                .archived(archived)
                .createdBy(adminId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .members(List.of(buildAdminMember()))
                .build();
    }

    private Group buildGroupWithMember(UUID memberUserId) {
        return Group.builder()
                .id(groupId)
                .name("Test Group")
                .joinPolicy(JoinPolicy.REQUEST)
                .archived(false)
                .createdBy(adminId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .members(List.of(
                        buildAdminMember(),
                        GroupMember.builder()
                                .id(UUID.randomUUID())
                                .groupId(groupId)
                                .userId(memberUserId)
                                .role(MemberRole.MEMBER)
                                .joinedAt(Instant.now())
                                .build()
                ))
                .build();
    }

    private GroupMember buildAdminMember() {
        return GroupMember.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .userId(adminId)
                .role(MemberRole.ADMIN)
                .joinedAt(Instant.now())
                .build();
    }
}
