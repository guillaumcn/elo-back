package com.elo.application.identity.port.in;

import com.elo.domain.identity.model.User;

import java.util.UUID;

public interface GetOwnProfilePort {

    User execute(UUID userId);
}
