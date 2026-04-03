package com.elo.infrastructure.adapter.in.web.group.mapper;

import com.elo.application.group.dto.GroupResponse;
import com.elo.domain.group.model.Group;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupResponseMapper {

    public static GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getJoinPolicy(),
                group.isArchived(),
                group.getCreatedBy(),
                group.getCreatedAt(),
                group.getMemberCount()
        );
    }
}
