package com.elo.application.group.usecase;

import com.elo.application.group.command.ListJoinRequestsCommand;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.exception.GroupAccessDeniedException;
import com.elo.domain.group.exception.GroupNotFoundException;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupJoinRequest;
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
class ListJoinRequestsUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupJoinRequestRepositoryPort joinRequestRepositoryPort;

    private ListJoinRequestsUseCase listJoinRequestsUseCase;

    private final UUID groupId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID memberUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listJoinRequestsUseCase = new ListJoinRequestsUseCase(groupRepositoryPort, joinRequestRepositoryPort);
    }

    @Test
    void shouldReturnPagedRequestsWhenUserIsAdmin() {
        Group group = buildGroupWithAdmin();
        GroupJoinRequest request = GroupJoinRequest.create(groupId, UUID.randomUUID());
        PagedResult<GroupJoinRequest> page = new PagedResult<>(List.of(request), 0, 20, 1L, 1);
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(joinRequestRepositoryPort.findAllByGroupId(groupId, 0, 20)).thenReturn(page);

        PagedResult<GroupJoinRequest> result = listJoinRequestsUseCase.execute(
                new ListJoinRequestsCommand(groupId, adminId, 0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listJoinRequestsUseCase.execute(
                new ListJoinRequestsCommand(groupId, adminId, 0, 20)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void shouldThrowAccessDeniedWhenUserIsNotAdmin() {
        Group group = buildGroupWithAdmin();
        when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> listJoinRequestsUseCase.execute(
                new ListJoinRequestsCommand(groupId, memberUserId, 0, 20)))
                .isInstanceOf(GroupAccessDeniedException.class);
    }

    private Group buildGroupWithAdmin() {
        return Group.builder()
                .id(groupId)
                .name("Test Group")
                .joinPolicy(JoinPolicy.REQUEST)
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
}
