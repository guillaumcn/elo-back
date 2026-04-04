package com.elo.infrastructure.adapter.out.persistence.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_invitations")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class GroupInvitationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Builder
    public GroupInvitationJpaEntity(UUID id, UUID groupId, String token, UUID invitedBy,
                                    Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.groupId = groupId;
        this.token = token;
        this.invitedBy = invitedBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
