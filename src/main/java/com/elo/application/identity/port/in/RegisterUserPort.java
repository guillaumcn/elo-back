package com.elo.application.identity.port.in;

import com.elo.application.identity.command.RegisterUserCommand;
import com.elo.domain.identity.model.User;

public interface RegisterUserPort {

    User execute(RegisterUserCommand command);
}
