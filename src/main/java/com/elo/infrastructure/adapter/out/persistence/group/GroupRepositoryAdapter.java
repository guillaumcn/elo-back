package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class GroupRepositoryAdapter implements GroupRepositoryPort {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;

    @Override
    public Group save(Group group) {
        GroupJpaEntity entity = GroupPersistenceMapper.toJpaEntity(group);
        GroupJpaEntity saved = groupJpaRepository.save(entity);
        int memberCount = groupMemberJpaRepository.countByGroupId(saved.getId());
        return GroupPersistenceMapper.toDomain(saved, memberCount);
    }

    @Override
    public Optional<Group> findById(UUID groupId) {
        return groupJpaRepository.findById(groupId).map(entity -> {
            int memberCount = groupMemberJpaRepository.countByGroupId(entity.getId());
            return GroupPersistenceMapper.toDomain(entity, memberCount);
        });
    }

    @Override
    public List<Group> findAllByMemberId(UUID userId) {
        List<UUID> groupIds = groupMemberJpaRepository.findGroupIdsByUserId(userId);
        return groupJpaRepository.findAllById(groupIds).stream()
                .map(entity -> {
                    int memberCount = groupMemberJpaRepository.countByGroupId(entity.getId());
                    return GroupPersistenceMapper.toDomain(entity, memberCount);
                })
                .toList();
    }
}
