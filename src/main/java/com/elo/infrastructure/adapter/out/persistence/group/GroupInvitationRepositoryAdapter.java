package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.domain.group.model.GroupInvitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupInvitationRepositoryAdapter implements GroupInvitationRepositoryPort {

    private final GroupInvitationJpaRepository groupInvitationJpaRepository;

    @Override
    public GroupInvitation save(GroupInvitation invitation) {
        GroupInvitationJpaEntity entity = GroupPersistenceMapper.toJpaEntity(invitation);
        GroupInvitationJpaEntity saved = groupInvitationJpaRepository.save(entity);
        return GroupPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<GroupInvitation> findByToken(String token) {
        return groupInvitationJpaRepository.findByToken(token)
                .map(GroupPersistenceMapper::toDomain);
    }
}
