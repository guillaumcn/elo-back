package com.elo.infrastructure.adapter.out.persistence.group;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {

    @Query(value = "SELECT g.* FROM groups g JOIN group_members m ON g.id = m.group_id WHERE m.user_id = :userId",
           countQuery = "SELECT COUNT(DISTINCT g.id) FROM groups g JOIN group_members m ON g.id = m.group_id WHERE m.user_id = :userId",
           nativeQuery = true)
    Page<GroupJpaEntity> findAllByMemberUserId(@Param("userId") UUID userId, Pageable pageable);
}
