package com.elo.infrastructure.adapter.out.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {
}
