package com.elo.domain.group.model;

import com.elo.domain.group.exception.GroupJoinRequestAlreadyResolvedException;
import com.elo.domain.group.exception.InvalidGroupException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupJoinRequestTest {

    private final UUID groupId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();

    @Test
    void shouldCreatePendingRequest() {
        GroupJoinRequest request = GroupJoinRequest.create(groupId, userId);

        assertThat(request.getId()).isNotNull();
        assertThat(request.getGroupId()).isEqualTo(groupId);
        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getStatus()).isEqualTo(GroupJoinRequestStatus.PENDING);
        assertThat(request.getRequestedAt()).isNotNull();
        assertThat(request.getResolvedBy()).isNull();
        assertThat(request.getResolvedAt()).isNull();
    }

    @Test
    void shouldThrowWhenCreatingWithNullGroupId() {
        assertThatThrownBy(() -> GroupJoinRequest.create(null, userId))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("Group ID is required");
    }

    @Test
    void shouldThrowWhenCreatingWithNullUserId() {
        assertThatThrownBy(() -> GroupJoinRequest.create(groupId, null))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("User ID is required");
    }

    @Test
    void shouldApprovePendingRequest() {
        GroupJoinRequest request = GroupJoinRequest.create(groupId, userId);

        request.approve(adminId);

        assertThat(request.getStatus()).isEqualTo(GroupJoinRequestStatus.APPROVED);
        assertThat(request.getResolvedBy()).isEqualTo(adminId);
        assertThat(request.getResolvedAt()).isNotNull();
    }

    @Test
    void shouldDenyPendingRequest() {
        GroupJoinRequest request = GroupJoinRequest.create(groupId, userId);

        request.deny(adminId);

        assertThat(request.getStatus()).isEqualTo(GroupJoinRequestStatus.DENIED);
        assertThat(request.getResolvedBy()).isEqualTo(adminId);
        assertThat(request.getResolvedAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenApprovingAlreadyApprovedRequest() {
        GroupJoinRequest request = GroupJoinRequest.create(groupId, userId);
        request.approve(adminId);

        assertThatThrownBy(() -> request.approve(adminId))
                .isInstanceOf(GroupJoinRequestAlreadyResolvedException.class);
    }

    @Test
    void shouldThrowWhenDenyingAlreadyDeniedRequest() {
        GroupJoinRequest request = GroupJoinRequest.create(groupId, userId);
        request.deny(adminId);

        assertThatThrownBy(() -> request.deny(adminId))
                .isInstanceOf(GroupJoinRequestAlreadyResolvedException.class);
    }

    @Test
    void shouldThrowWhenDenyingAlreadyApprovedRequest() {
        GroupJoinRequest request = GroupJoinRequest.create(groupId, userId);
        request.approve(adminId);

        assertThatThrownBy(() -> request.deny(adminId))
                .isInstanceOf(GroupJoinRequestAlreadyResolvedException.class);
    }
}
