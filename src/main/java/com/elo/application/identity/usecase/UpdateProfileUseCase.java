package com.elo.application.identity.usecase;

import com.elo.application.identity.command.UpdateProfileCommand;
import com.elo.application.identity.port.in.UpdateProfilePort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.UserNotFoundException;
import com.elo.domain.identity.exception.UsernameAlreadyTakenException;
import com.elo.domain.identity.model.User;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateProfileUseCase implements UpdateProfilePort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User execute(UpdateProfileCommand command) {
        User user = findUserById(command.userId());
        if (isUsernameChanging(command.username(), user.getUsername())) {
            ensureUsernameIsAvailable(command.username());
        }
        user.updateProfile(command.username(), command.avatarUrl(), command.bio());
        return userRepositoryPort.save(user);
    }

    private User findUserById(UUID userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    private boolean isUsernameChanging(String newUsername, String currentUsername) {
        return newUsername != null && !newUsername.equals(currentUsername);
    }

    private void ensureUsernameIsAvailable(String username) {
        if (userRepositoryPort.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
        }
    }
}
