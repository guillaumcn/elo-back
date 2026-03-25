package com.elo.application.identity.usecase;

import com.elo.application.identity.command.GetPublicProfileCommand;
import com.elo.application.identity.port.in.GetPublicProfilePort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.application.identity.port.out.UserVisibilityPort;
import com.elo.domain.identity.exception.UserNotFoundException;
import com.elo.domain.identity.model.User;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetPublicProfileUseCase implements GetPublicProfilePort {

    private final UserRepositoryPort userRepositoryPort;
    private final UserVisibilityPort userVisibilityPort;

    @Override
    public User execute(GetPublicProfileCommand command) {
        User target = findActiveUserByUsername(command.targetUsername());
        ensureUserIsVisible(command.requesterId(), target, command.targetUsername());
        return target;
    }

    private User findActiveUserByUsername(String username) {
        return userRepositoryPort.findByUsername(username)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private void ensureUserIsVisible(UUID requesterId, User target, String username) {
        if (!userVisibilityPort.isVisible(requesterId, target.getId())) {
            throw new UserNotFoundException(username);
        }
    }
}
