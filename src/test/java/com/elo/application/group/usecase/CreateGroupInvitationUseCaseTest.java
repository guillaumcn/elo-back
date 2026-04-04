package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupInvitationCommand;
import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
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
class CreateGroupInvitationUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupInvitationRepositoryPort groupInvitationRepositoryPort;

    private CreateGroupInvitationUseCase createGroupInvitationUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID invitedBy = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createGroupInvitationUseCase = new CreateGroupInvitationUseCase(
                groupRepositoryPort, groupInvitationRepositoryPort);
    }

    @Test
    void shouldCreateInvitationWhenRequesterIsMember() {
        Group group = buildGroup();
        GroupInvitation savedInvitation = buildInvitation(null);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupInvitationRepositoryPort.save(any(GroupInvitation.class))).thenReturn(savedInvitation);

        GroupInvitation result = createGroupInvitationUseCase.execute(
                new CreateGroupInvitationCommand(groupId, invitedBy, null));

        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getInvitedBy()).isEqualTo(invitedBy);
        verify(groupInvitationRepositoryPort).save(any(GroupInvitation.class));
    }

    @Test
    void shouldCreateInvitationWithExpirationDate() {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Group group = buildGroup();
        GroupInvitation savedInvitation = buildInvitation(expiresAt);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupInvitationRepositoryPort.save(any(GroupInvitation.class))).thenReturn(savedInvitation);

        GroupInvitation result = createGroupInvitationUseCase.execute(
                new CreateGroupInvitationCommand(groupId, invitedBy, expiresAt));

        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createGroupInvitationUseCase.execute(
                new CreateGroupInvitationCommand(groupId, invitedBy, null)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenRequesterIsNotMember() {
        UUID nonMemberId = UUID.randomUUID();
        Group group = buildGroup();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> createGroupInvitationUseCase.execute(
                new CreateGroupInvitationCommand(groupId, nonMemberId, null)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    private Group buildGroup() {
        return Group.builder()
                .id(groupId)
                .name("Chess Club")
                .joinPolicy(JoinPolicy.INVITATION)
                .archived(false)
                .createdBy(invitedBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .members(List.of(GroupMember.builder()
                        .id(UUID.randomUUID())
                        .groupId(groupId)
                        .userId(invitedBy)
                        .role(MemberRole.ADMIN)
                        .joinedAt(Instant.now())
                        .build()))
                .build();
    }

    private GroupInvitation buildInvitation(Instant expiresAt) {
        return GroupInvitation.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .token(UUID.randomUUID().toString())
                .invitedBy(invitedBy)
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .build();
    }
}
