package com.elo.application.group.usecase;

import com.elo.application.group.command.GetGroupCommand;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.JoinPolicy;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupMemberRepositoryPort groupMemberRepositoryPort;

    private GetGroupUseCase getGroupUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getGroupUseCase = new GetGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Test
    void shouldReturnGroupWhenUserIsMember() {
        Group group = buildGroup();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, userId)).thenReturn(true);

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
        when(groupMemberRepositoryPort.existsByGroupIdAndUserId(groupId, userId)).thenReturn(false);

        assertThatThrownBy(() -> getGroupUseCase.execute(new GetGroupCommand(groupId, userId)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    private Group buildGroup() {
        return Group.builder()
                .id(groupId)
                .name("Ping Pong Club")
                .joinPolicy(JoinPolicy.OPEN)
                .archived(false)
                .createdBy(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .memberCount(1)
                .build();
    }
}
