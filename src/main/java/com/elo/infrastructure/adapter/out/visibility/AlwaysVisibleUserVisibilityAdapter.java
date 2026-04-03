package com.elo.infrastructure.adapter.out.visibility;

import com.elo.application.identity.port.out.UserVisibilityPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stub implementation — always returns true until the Group context implements group-based visibility.
 * Not active in production to prevent accidentally exposing all user data.
 */
@Component
@Profile("!prod")
public class AlwaysVisibleUserVisibilityAdapter implements UserVisibilityPort {

    @Override
    public boolean isVisible(UUID requesterId, UUID targetUserId) {
        return true;
    }
}
