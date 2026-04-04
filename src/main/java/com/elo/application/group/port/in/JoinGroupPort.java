package com.elo.application.group.port.in;

import com.elo.application.group.command.JoinGroupCommand;
import com.elo.domain.group.model.GroupMember;

public interface JoinGroupPort {

    GroupMember execute(JoinGroupCommand command);
}
