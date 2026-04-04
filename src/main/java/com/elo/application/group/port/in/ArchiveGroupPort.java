package com.elo.application.group.port.in;

import com.elo.application.group.command.ArchiveGroupCommand;
import com.elo.domain.group.model.Group;

public interface ArchiveGroupPort {
    Group execute(ArchiveGroupCommand command);
}
