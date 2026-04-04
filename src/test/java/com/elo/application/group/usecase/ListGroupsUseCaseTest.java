package com.elo.application.group.usecase;

import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.shared.PagedResult;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListGroupsUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    private ListGroupsUseCase listGroupsUseCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listGroupsUseCase = new ListGroupsUseCase(groupRepositoryPort);
    }

    @Test
    void shouldReturnGroupsForUser() {
        List<Group> groups = List.of(buildGroup("Ping Pong Club"), buildGroup("Chess Club"));
        when(groupRepositoryPort.findAllByMemberId(userId, 0, 20)).thenReturn(new PagedResult<>(groups, 0, 20, 2L, 1));

        PagedResult<Group> result = listGroupsUseCase.execute(userId, 0, 20);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).extracting(Group::getName).containsExactlyInAnyOrder("Ping Pong Club", "Chess Club");
        assertThat(result.totalElements()).isEqualTo(2L);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGroups() {
        when(groupRepositoryPort.findAllByMemberId(userId, 0, 20)).thenReturn(new PagedResult<>(List.of(), 0, 20, 0L, 0));

        PagedResult<Group> result = listGroupsUseCase.execute(userId, 0, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    private Group buildGroup(String name) {
        UUID groupId = UUID.randomUUID();
        return Group.builder()
                .id(groupId)
                .name(name)
                .joinPolicy(JoinPolicy.OPEN)
                .archived(false)
                .createdBy(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .members(List.of(GroupMember.builder()
                        .id(UUID.randomUUID())
                        .groupId(groupId)
                        .userId(userId)
                        .role(MemberRole.ADMIN)
                        .joinedAt(Instant.now())
                        .build()))
                .build();
    }
}
