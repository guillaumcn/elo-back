package com.elo.application.identity.port.in;

import com.elo.application.identity.command.UpdateProfileCommand;
import com.elo.domain.identity.model.User;

public interface UpdateProfilePort {

    User execute(UpdateProfileCommand command);
}
