package com.elo.application.group.usecase;

import com.elo.application.group.command.UnarchiveGroupCommand;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotArchivedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
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
class UnarchiveGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupMemberRepositoryPort groupMemberRepositoryPort;

    private UnarchiveGroupUseCase unarchiveGroupUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        unarchiveGroupUseCase = new UnarchiveGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Test
    void shouldUnarchiveGroupWhenRequesterIsAdmin() {
        Group group = buildGroup(true);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, adminId, MemberRole.ADMIN)).thenReturn(true);
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        Group result = unarchiveGroupUseCase.execute(new UnarchiveGroupCommand(groupId, adminId));

        assertThat(result.isArchived()).isFalse();
        verify(groupRepositoryPort).save(group);
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unarchiveGroupUseCase.execute(new UnarchiveGroupCommand(groupId, adminId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenRequesterIsNotMember() {
        Group group = buildGroup(true);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, memberId, MemberRole.ADMIN)).thenReturn(false);
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, memberId)).thenReturn(false);

        assertThatThrownBy(() -> unarchiveGroupUseCase.execute(new UnarchiveGroupCommand(groupId, memberId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenRequesterIsNotAdmin() {
        Group group = buildGroup(true);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, memberId, MemberRole.ADMIN)).thenReturn(false);
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, memberId)).thenReturn(true);

        assertThatThrownBy(() -> unarchiveGroupUseCase.execute(new UnarchiveGroupCommand(groupId, memberId)))
                .isInstanceOf(GroupAccessDeniedException.class);
    }

    @Test
    void shouldThrowWhenGroupIsNotArchived() {
        Group group = buildGroup(false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, adminId, MemberRole.ADMIN)).thenReturn(true);

        assertThatThrownBy(() -> unarchiveGroupUseCase.execute(new UnarchiveGroupCommand(groupId, adminId)))
                .isInstanceOf(GroupNotArchivedException.class);
    }

    private Group buildGroup(boolean archived) {
        return Group.builder()
                .id(groupId)
                .name("Ping Pong Club")
                .joinPolicy(JoinPolicy.OPEN)
                .archived(archived)
                .createdBy(adminId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .memberCount(2)
                .build();
    }
}
