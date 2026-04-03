package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepositoryAdapter implements GroupMemberRepositoryPort {

    private final GroupMemberJpaRepository groupMemberJpaRepository;

    @Override
    public GroupMember save(GroupMember member) {
        GroupMemberJpaEntity entity = GroupPersistenceMapper.toJpaEntity(member);
        GroupMemberJpaEntity saved = groupMemberJpaRepository.save(entity);
        return GroupPersistenceMapper.toDomain(saved);
    }

    @Override
    public boolean existsByGroupIdAndUserId(UUID groupId, UUID userId) {
        return groupMemberJpaRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public boolean existsByGroupIdAndUserIdAndRole(UUID groupId, UUID userId, MemberRole role) {
        return groupMemberJpaRepository.existsByGroupIdAndUserIdAndRole(groupId, userId, role);
    }
}
