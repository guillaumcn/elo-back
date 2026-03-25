package com.elo.application.identity.port.out;

import java.util.UUID;

public interface UserVisibilityPort {

    boolean isVisible(UUID requesterId, UUID targetUserId);
}
