package com.elo.application.group.port.out;

import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.MemberRole;

import java.util.UUID;

public interface GroupMemberRepositoryPort {

    GroupMember save(GroupMember member);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserIdAndRole(UUID groupId, UUID userId, MemberRole role);
}
