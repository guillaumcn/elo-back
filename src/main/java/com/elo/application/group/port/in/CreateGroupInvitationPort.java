package com.elo.application.group.port.in;

import com.elo.application.group.command.CreateGroupInvitationCommand;
import com.elo.domain.group.model.GroupInvitation;

public interface CreateGroupInvitationPort {

    GroupInvitation execute(CreateGroupInvitationCommand command);
}
