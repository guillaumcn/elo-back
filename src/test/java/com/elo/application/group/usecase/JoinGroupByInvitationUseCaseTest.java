package com.elo.application.group.usecase;

import com.elo.application.group.command.JoinGroupByInvitationCommand;
import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupInvitationExpiredException;
import com.elo.domain.group.exception.GroupInvitationNotFoundException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupInvitation;
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
class JoinGroupByInvitationUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupInvitationRepositoryPort groupInvitationRepositoryPort;

    private JoinGroupByInvitationUseCase joinGroupByInvitationUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final String token = "valid-token";

    @BeforeEach
    void setUp() {
        joinGroupByInvitationUseCase = new JoinGroupByInvitationUseCase(
                groupRepositoryPort, groupInvitationRepositoryPort);
    }

    @Test
    void shouldJoinGroupByValidInvitation() {
        GroupInvitation invitation = buildInvitation(null);
        Group group = buildGroup(false);
        when(groupInvitationRepositoryPort.findByToken(token)).thenReturn(Optional.of(invitation));
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        GroupMember result = joinGroupByInvitationUseCase.execute(new JoinGroupByInvitationCommand(groupId, token, userId));

        assertThat(result.getRole()).isEqualTo(MemberRole.MEMBER);
        verify(groupRepositoryPort).save(any(Group.class));
    }

    @Test
    void shouldThrowNotFoundWhenTokenDoesNotExist() {
        when(groupInvitationRepositoryPort.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> joinGroupByInvitationUseCase.execute(
                new JoinGroupByInvitationCommand(groupId, token, userId)))
                .isInstanceOf(GroupInvitationNotFoundException.class);
    }

    @Test
    void shouldThrowWhenInvitationIsExpired() {
        GroupInvitation expiredInvitation = buildInvitation(Instant.now().minusSeconds(1));
        when(groupInvitationRepositoryPort.findByToken(token)).thenReturn(Optional.of(expiredInvitation));

        assertThatThrownBy(() -> joinGroupByInvitationUseCase.execute(
                new JoinGroupByInvitationCommand(groupId, token, userId)))
                .isInstanceOf(GroupInvitationExpiredException.class);
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        GroupInvitation invitation = buildInvitation(null);
        when(groupInvitationRepositoryPort.findByToken(token)).thenReturn(Optional.of(invitation));
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> joinGroupByInvitationUseCase.execute(
                new JoinGroupByInvitationCommand(groupId, token, userId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowWhenGroupIsArchived() {
        GroupInvitation invitation = buildInvitation(null);
        Group group = buildGroup(true);
        when(groupInvitationRepositoryPort.findByToken(token)).thenReturn(Optional.of(invitation));
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> joinGroupByInvitationUseCase.execute(
                new JoinGroupByInvitationCommand(groupId, token, userId)))
                .isInstanceOf(GroupAlreadyArchivedException.class);
    }

    @Test
    void shouldThrowWhenUserIsAlreadyMember() {
        GroupInvitation invitation = buildInvitation(null);
        Group group = buildGroupWithMember(userId);
        when(groupInvitationRepositoryPort.findByToken(token)).thenReturn(Optional.of(invitation));
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> joinGroupByInvitationUseCase.execute(
                new JoinGroupByInvitationCommand(groupId, token, userId)))
                .isInstanceOf(GroupAlreadyMemberException.class);
    }

    private GroupInvitation buildInvitation(Instant expiresAt) {
        return GroupInvitation.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .token(token)
                .invitedBy(UUID.randomUUID())
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .build();
    }

    private Group buildGroup(boolean archived) {
        return Group.builder()
                .id(groupId)
                .name("Chess Club")
                .joinPolicy(JoinPolicy.INVITATION)
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
                .name("Chess Club")
                .joinPolicy(JoinPolicy.INVITATION)
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
