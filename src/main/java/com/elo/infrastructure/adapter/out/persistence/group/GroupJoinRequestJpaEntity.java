package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.domain.group.model.GroupJoinRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_join_requests")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class GroupJoinRequestJpaEntity {

    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupJoinRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Builder
    public GroupJoinRequestJpaEntity(UUID id, UUID groupId, UUID userId, GroupJoinRequestStatus status,
                                     Instant requestedAt, UUID resolvedBy, Instant resolvedAt) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }
}
