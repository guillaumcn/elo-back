package com.elo.application.group.port.in;

import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.Group;

import java.util.UUID;

public interface ListGroupsPort {

    PagedResult<Group> execute(UUID userId, int page, int size);
}
