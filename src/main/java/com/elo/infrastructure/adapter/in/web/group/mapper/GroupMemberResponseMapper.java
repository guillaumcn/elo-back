package com.elo.infrastructure.adapter.in.web.group.mapper;

import com.elo.application.group.dto.GroupMemberResponse;
import com.elo.domain.group.model.GroupMember;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupMemberResponseMapper {

    public static GroupMemberResponse toResponse(GroupMember member) {
        return new GroupMemberResponse(
                member.getId(),
                member.getGroupId(),
                member.getUserId(),
                member.getRole().name(),
                member.getJoinedAt()
        );
    }
}
