package com.elo.application.group.port.in;

import com.elo.application.group.command.UnarchiveGroupCommand;
import com.elo.domain.group.model.Group;

public interface UnarchiveGroupPort {
    Group execute(UnarchiveGroupCommand command);
}
