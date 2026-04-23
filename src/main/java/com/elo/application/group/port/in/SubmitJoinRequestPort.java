package com.elo.application.group.port.in;

import com.elo.application.group.command.SubmitJoinRequestCommand;
import com.elo.domain.group.model.GroupJoinRequest;

public interface SubmitJoinRequestPort {
    GroupJoinRequest execute(SubmitJoinRequestCommand command);
}
