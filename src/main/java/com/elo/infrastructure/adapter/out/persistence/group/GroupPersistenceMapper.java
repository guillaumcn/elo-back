package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupMember;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupPersistenceMapper {

    public static GroupJpaEntity toJpaEntity(Group group) {
        return GroupJpaEntity.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .joinPolicy(group.getJoinPolicy())
                .archived(group.isArchived())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    public static Group toDomain(GroupJpaEntity entity, int memberCount) {
        return Group.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .joinPolicy(entity.getJoinPolicy())
                .archived(entity.isArchived())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .memberCount(memberCount)
                .build();
    }

    public static GroupMemberJpaEntity toJpaEntity(GroupMember member) {
        return GroupMemberJpaEntity.builder()
                .id(member.getId())
                .groupId(member.getGroupId())
                .userId(member.getUserId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public static GroupMember toDomain(GroupMemberJpaEntity entity) {
        return GroupMember.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .userId(entity.getUserId())
                .role(entity.getRole())
                .joinedAt(entity.getJoinedAt())
                .build();
    }
}
