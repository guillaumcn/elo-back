package com.elo.application.group.port.in;

import com.elo.application.group.command.JoinGroupByInvitationCommand;
import com.elo.domain.group.model.GroupMember;

public interface JoinGroupByInvitationPort {

    GroupMember execute(JoinGroupByInvitationCommand command);
}
