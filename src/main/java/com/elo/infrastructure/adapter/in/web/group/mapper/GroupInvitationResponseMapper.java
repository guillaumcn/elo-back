package com.elo.infrastructure.adapter.in.web.group.mapper;

import com.elo.application.group.dto.GroupInvitationResponse;
import com.elo.domain.group.model.GroupInvitation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupInvitationResponseMapper {

    public static GroupInvitationResponse toResponse(GroupInvitation invitation) {
        return new GroupInvitationResponse(
                invitation.getId(),
                invitation.getGroupId(),
                invitation.getToken(),
                invitation.getInvitedBy(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt()
        );
    }
}
