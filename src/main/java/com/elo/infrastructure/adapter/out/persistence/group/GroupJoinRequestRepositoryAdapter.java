package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.GroupJoinRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupJoinRequestRepositoryAdapter implements GroupJoinRequestRepositoryPort {

    private final GroupJoinRequestJpaRepository joinRequestJpaRepository;

    @Override
    @Transactional
    public GroupJoinRequest save(GroupJoinRequest joinRequest) {
        GroupJoinRequestJpaEntity entity = GroupPersistenceMapper.toJpaEntity(joinRequest);
        GroupJoinRequestJpaEntity saved = joinRequestJpaRepository.save(entity);
        return GroupPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupJoinRequest> findById(UUID id) {
        return joinRequestJpaRepository.findById(id)
                .map(GroupPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupJoinRequest> findPendingByGroupIdAndUserId(UUID groupId, UUID userId) {
        return joinRequestJpaRepository.findByGroupIdAndUserIdAndStatus(groupId, userId, GroupJoinRequestStatus.PENDING)
                .map(GroupPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<GroupJoinRequest> findAllByGroupId(UUID groupId, int page, int size) {
        Page<GroupJoinRequestJpaEntity> entityPage = joinRequestJpaRepository.findAllByGroupId(groupId, PageRequest.of(page, size));
        List<GroupJoinRequest> content = entityPage.getContent().stream()
                .map(GroupPersistenceMapper::toDomain)
                .toList();
        return new PagedResult<>(content, page, size, entityPage.getTotalElements(), entityPage.getTotalPages());
    }
}
