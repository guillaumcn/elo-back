package com.elo.infrastructure.adapter.in.web.group.mapper;

import com.elo.application.group.dto.GroupJoinRequestResponse;
import com.elo.domain.group.model.GroupJoinRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupJoinRequestResponseMapper {

    public static GroupJoinRequestResponse toResponse(GroupJoinRequest joinRequest) {
        return new GroupJoinRequestResponse(
                joinRequest.getId(),
                joinRequest.getGroupId(),
                joinRequest.getUserId(),
                joinRequest.getStatus(),
                joinRequest.getRequestedAt(),
                joinRequest.getResolvedBy(),
                joinRequest.getResolvedAt()
        );
    }
}
