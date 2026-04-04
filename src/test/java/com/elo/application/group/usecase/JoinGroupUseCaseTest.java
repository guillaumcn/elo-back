package com.elo.application.group.usecase;

import com.elo.application.group.command.JoinGroupCommand;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupJoinPolicyViolationException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.JoinPolicy;
import com.elo.domain.group.model.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoinGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupMemberRepositoryPort groupMemberRepositoryPort;

    private JoinGroupUseCase joinGroupUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        joinGroupUseCase = new JoinGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Test
    void shouldJoinOpenGroup() {
        Group group = buildGroup(JoinPolicy.OPEN, false);
        GroupMember savedMember = buildMember();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, userId)).thenReturn(false);
        when(groupMemberRepositoryPort.save(any(GroupMember.class))).thenReturn(savedMember);

        GroupMember result = joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId));

        assertThat(result.getRole()).isEqualTo(MemberRole.MEMBER);
        verify(groupMemberRepositoryPort).save(any(GroupMember.class));
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowWhenGroupIsArchived() {
        Group group = buildGroup(JoinPolicy.OPEN, true);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId)))
                .isInstanceOf(GroupAlreadyArchivedException.class);
    }

    @Test
    void shouldThrowWhenGroupIsNotOpen() {
        Group group = buildGroup(JoinPolicy.INVITATION, false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId)))
                .isInstanceOf(GroupJoinPolicyViolationException.class);
    }

    @Test
    void shouldThrowWhenUserIsAlreadyMember() {
        Group group = buildGroup(JoinPolicy.OPEN, false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, userId)).thenReturn(true);

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId)))
                .isInstanceOf(GroupAlreadyMemberException.class);
    }

    private Group buildGroup(JoinPolicy joinPolicy, boolean archived) {
        return Group.builder()
                .id(groupId)
                .name("Test Group")
                .joinPolicy(joinPolicy)
                .archived(archived)
                .createdBy(UUID.randomUUID())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .memberCount(1)
                .build();
    }

    private GroupMember buildMember() {
        return GroupMember.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .userId(userId)
                .role(MemberRole.MEMBER)
                .joinedAt(Instant.now())
                .build();
    }
}
