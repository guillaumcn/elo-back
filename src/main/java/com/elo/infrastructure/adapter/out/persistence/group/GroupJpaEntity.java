package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.domain.group.model.JoinPolicy;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class GroupJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_policy", nullable = false)
    private JoinPolicy joinPolicy;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroupMemberJpaEntity> members = new ArrayList<>();

    @Builder
    public GroupJpaEntity(UUID id, String name, String description, JoinPolicy joinPolicy,
                          boolean archived, UUID createdBy, Instant createdAt, Instant updatedAt,
                          List<GroupMemberJpaEntity> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.joinPolicy = joinPolicy;
        this.archived = archived;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
    }
}
