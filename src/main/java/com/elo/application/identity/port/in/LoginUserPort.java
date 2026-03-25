package com.elo.application.identity.port.in;

import com.elo.application.identity.command.LoginCommand;
import com.elo.domain.identity.model.User;

public interface LoginUserPort {

    User execute(LoginCommand command);
}
