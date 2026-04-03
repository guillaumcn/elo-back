package com.elo.application.group.port.in;

import com.elo.application.group.command.CreateGroupCommand;
import com.elo.domain.group.model.Group;

public interface CreateGroupPort {

    Group execute(CreateGroupCommand command);
}
