package com.elo.application.group.usecase;

import com.elo.application.group.command.JoinGroupCommand;
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
import java.util.List;
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

    private JoinGroupUseCase joinGroupUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        joinGroupUseCase = new JoinGroupUseCase(groupRepositoryPort);
    }

    @Test
    void shouldJoinOpenGroup() {
        Group group = buildGroup(JoinPolicy.OPEN, false);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        GroupMember result = joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId));

        assertThat(result.getRole()).isEqualTo(MemberRole.MEMBER);
        verify(groupRepositoryPort).save(any(Group.class));
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
        Group group = buildGroupWithMember(userId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupCommand(groupId, userId)))
                .isInstanceOf(GroupAlreadyMemberException.class);
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
                .joinPolicy(JoinPolicy.OPEN)
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
