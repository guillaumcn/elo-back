package com.elo.application.group.usecase;

import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.JoinPolicy;
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
        when(groupRepositoryPort.findAllByMemberId(userId)).thenReturn(groups);

        List<Group> result = listGroupsUseCase.execute(userId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Group::getName).containsExactlyInAnyOrder("Ping Pong Club", "Chess Club");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGroups() {
        when(groupRepositoryPort.findAllByMemberId(userId)).thenReturn(List.of());

        List<Group> result = listGroupsUseCase.execute(userId);

        assertThat(result).isEmpty();
    }

    private Group buildGroup(String name) {
        return Group.builder()
                .id(UUID.randomUUID())
                .name(name)
                .joinPolicy(JoinPolicy.OPEN)
                .archived(false)
                .createdBy(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .memberCount(1)
                .build();
    }
}
