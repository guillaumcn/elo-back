package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.domain.group.model.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, UUID> {

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserIdAndRole(UUID groupId, UUID userId, MemberRole role);

    int countByGroupId(UUID groupId);

    @Query("SELECT m.groupId FROM GroupMemberJpaEntity m WHERE m.userId = :userId")
    List<UUID> findGroupIdsByUserId(@Param("userId") UUID userId);
}
