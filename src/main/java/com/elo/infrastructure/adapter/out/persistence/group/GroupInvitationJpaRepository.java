package com.elo.infrastructure.adapter.out.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupInvitationJpaRepository extends JpaRepository<GroupInvitationJpaEntity, UUID> {

    Optional<GroupInvitationJpaEntity> findByToken(String token);
}
