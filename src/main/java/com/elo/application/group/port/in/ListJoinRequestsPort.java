package com.elo.application.group.port.in;

import com.elo.application.group.command.ListJoinRequestsCommand;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.GroupJoinRequest;

public interface ListJoinRequestsPort {
    PagedResult<GroupJoinRequest> execute(ListJoinRequestsCommand command);
}
