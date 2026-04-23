package com.elo.infrastructure.adapter.out.persistence.group;

import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupInvitation;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.GroupMember;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupPersistenceMapper {

    public static GroupJpaEntity toJpaEntity(Group group) {
        List<GroupMemberJpaEntity> memberEntities = group.getMembers().stream()
                .map(GroupPersistenceMapper::toJpaEntity)
                .toList();
        return GroupJpaEntity.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .joinPolicy(group.getJoinPolicy())
                .archived(group.isArchived())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .members(memberEntities)
                .build();
    }

    public static Group toDomain(GroupJpaEntity entity) {
        List<GroupMember> members = entity.getMembers().stream()
                .map(GroupPersistenceMapper::toDomain)
                .toList();
        return Group.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .joinPolicy(entity.getJoinPolicy())
                .archived(entity.isArchived())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .members(members)
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

    public static GroupInvitationJpaEntity toJpaEntity(GroupInvitation invitation) {
        return GroupInvitationJpaEntity.builder()
                .id(invitation.getId())
                .groupId(invitation.getGroupId())
                .token(invitation.getToken())
                .invitedBy(invitation.getInvitedBy())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }

    public static GroupInvitation toDomain(GroupInvitationJpaEntity entity) {
        return GroupInvitation.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .token(entity.getToken())
                .invitedBy(entity.getInvitedBy())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    public static GroupJoinRequestJpaEntity toJpaEntity(GroupJoinRequest joinRequest) {
        return GroupJoinRequestJpaEntity.builder()
                .id(joinRequest.getId())
                .groupId(joinRequest.getGroupId())
                .userId(joinRequest.getUserId())
                .status(joinRequest.getStatus())
                .requestedAt(joinRequest.getRequestedAt())
                .resolvedBy(joinRequest.getResolvedBy())
                .resolvedAt(joinRequest.getResolvedAt())
                .build();
    }

    public static GroupJoinRequest toDomain(GroupJoinRequestJpaEntity entity) {
        return GroupJoinRequest.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .resolvedBy(entity.getResolvedBy())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
