package com.elo.application.identity.port.in;

import com.elo.application.identity.command.GetPublicProfileCommand;
import com.elo.domain.identity.model.User;

public interface GetPublicProfilePort {

    User execute(GetPublicProfileCommand command);
}
