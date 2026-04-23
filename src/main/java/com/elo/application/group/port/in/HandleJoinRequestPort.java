package com.elo.application.group.port.in;

import com.elo.application.group.command.HandleJoinRequestCommand;
import com.elo.domain.group.model.GroupJoinRequest;

public interface HandleJoinRequestPort {
    GroupJoinRequest execute(HandleJoinRequestCommand command);
}
