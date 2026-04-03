package com.elo.application.group.port.in;

import com.elo.application.group.command.GetGroupCommand;
import com.elo.domain.group.model.Group;

public interface GetGroupPort {

    Group execute(GetGroupCommand command);
}
