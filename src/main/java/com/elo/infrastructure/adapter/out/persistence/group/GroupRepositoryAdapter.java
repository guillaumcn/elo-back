package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.Group;
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
public class GroupRepositoryAdapter implements GroupRepositoryPort {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;

    @Override
    @Transactional
    public Group save(Group group) {
        GroupJpaEntity entity = GroupPersistenceMapper.toJpaEntity(group);
        groupMemberJpaRepository.deleteByGroupId(group.getId());
        GroupJpaEntity saved = groupJpaRepository.save(entity);
        groupMemberJpaRepository.saveAll(entity.getMembers());
        return GroupPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Group> findById(UUID groupId) {
        return groupJpaRepository.findById(groupId)
                .map(GroupPersistenceMapper::toDomain);
    }

    @Override
    public PagedResult<Group> findAllByMemberId(UUID userId, int page, int size) {
        Page<GroupJpaEntity> groupPage = groupJpaRepository.findAllByMemberUserId(userId, PageRequest.of(page, size));
        List<Group> groups = groupPage.getContent().stream()
                .map(GroupPersistenceMapper::toDomain)
                .toList();
        return new PagedResult<>(groups, page, size, groupPage.getTotalElements(), groupPage.getTotalPages());
    }
}
