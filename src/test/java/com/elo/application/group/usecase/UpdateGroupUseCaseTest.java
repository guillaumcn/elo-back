package com.elo.application.group.usecase;

import com.elo.application.group.command.UpdateGroupCommand;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAccessDeniedException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupMemberRepositoryPort groupMemberRepositoryPort;

    private UpdateGroupUseCase updateGroupUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        updateGroupUseCase = new UpdateGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Test
    void shouldUpdateGroupWhenRequesterIsAdmin() {
        Group group = buildGroup();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, adminId, MemberRole.ADMIN)).thenReturn(true);
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        updateGroupUseCase.execute(new UpdateGroupCommand(groupId, adminId, "New Name", null, null));

        verify(groupRepositoryPort).save(any(Group.class));
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateGroupUseCase.execute(
                new UpdateGroupCommand(groupId, adminId, "New Name", null, null)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenRequesterIsNotMember() {
        Group group = buildGroup();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, memberId, MemberRole.ADMIN)).thenReturn(false);
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, memberId)).thenReturn(false);

        assertThatThrownBy(() -> updateGroupUseCase.execute(
                new UpdateGroupCommand(groupId, memberId, "New Name", null, null)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenRequesterIsNotAdmin() {
        Group group = buildGroup();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole(groupId, memberId, MemberRole.ADMIN)).thenReturn(false);
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, memberId)).thenReturn(true);

        assertThatThrownBy(() -> updateGroupUseCase.execute(
                new UpdateGroupCommand(groupId, memberId, "New Name", null, null)))
                .isInstanceOf(GroupAccessDeniedException.class);
    }

    private Group buildGroup() {
        return Group.builder()
                .id(groupId)
                .name("Ping Pong Club")
                .joinPolicy(JoinPolicy.OPEN)
                .archived(false)
                .createdBy(adminId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .memberCount(2)
                .build();
    }
}
