package com.elo.application.group.usecase;

import com.elo.application.group.command.GetGroupCommand;
import com.elo.application.group.port.out.GroupRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    private GetGroupUseCase getGroupUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getGroupUseCase = new GetGroupUseCase(groupRepositoryPort);
    }

    @Test
    void shouldReturnGroupWhenUserIsMember() {
        Group group = buildGroupWithMember(userId);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        Group result = getGroupUseCase.execute(new GetGroupCommand(groupId, userId));

        assertThat(result.getId()).isEqualTo(groupId);
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getGroupUseCase.execute(new GetGroupCommand(groupId, userId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenUserIsNotMember() {
        Group group = buildGroup();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> getGroupUseCase.execute(new GetGroupCommand(groupId, userId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    private Group buildGroup() {
        UUID adminId = UUID.randomUUID();
        return Group.builder()
                .id(groupId)
                .name("Ping Pong Club")
                .joinPolicy(JoinPolicy.OPEN)
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

    private Group buildGroupWithMember(UUID memberUserId) {
        UUID adminId = UUID.randomUUID();
        return Group.builder()
                .id(groupId)
                .name("Ping Pong Club")
                .joinPolicy(JoinPolicy.OPEN)
                .archived(false)
                .createdBy(adminId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .members(List.of(
                        GroupMember.builder()
                                .id(UUID.randomUUID())
                                .groupId(groupId)
                                .userId(adminId)
                                .role(MemberRole.ADMIN)
                                .joinedAt(Instant.now())
                                .build(),
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
}
