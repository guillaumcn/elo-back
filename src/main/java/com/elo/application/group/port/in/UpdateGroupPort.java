package com.elo.application.group.port.in;

import com.elo.application.group.command.UpdateGroupCommand;
import com.elo.domain.group.model.Group;

public interface UpdateGroupPort {

    Group execute(UpdateGroupCommand command);
}
