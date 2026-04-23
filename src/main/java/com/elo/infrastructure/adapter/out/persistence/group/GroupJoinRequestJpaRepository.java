package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.domain.group.model.GroupJoinRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupJoinRequestJpaRepository extends JpaRepository<GroupJoinRequestJpaEntity, UUID> {
    Optional<GroupJoinRequestJpaEntity> findByGroupIdAndUserIdAndStatus(UUID groupId, UUID userId, GroupJoinRequestStatus status);
    Page<GroupJoinRequestJpaEntity> findAllByGroupId(UUID groupId, Pageable pageable);
}
