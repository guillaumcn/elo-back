package com.elo.application.identity.usecase;

import com.elo.application.identity.command.RegisterUserCommand;
import com.elo.application.identity.port.in.RegisterUserPort;
import com.elo.application.identity.port.out.PasswordHasherPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.EmailAlreadyTakenException;
import com.elo.domain.identity.exception.UsernameAlreadyTakenException;
import com.elo.domain.identity.model.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUserUseCase implements RegisterUserPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasher;

    @Override
    public User execute(RegisterUserCommand command) {
        ensureUsernameIsAvailable(command.username());
        ensureEmailIsAvailable(command.email());

        String hashedPassword = passwordHasher.hash(command.rawPassword());
        User user = User.create(command.username(), command.email(), hashedPassword);
        return userRepositoryPort.save(user);
    }

    private void ensureUsernameIsAvailable(String username) {
        if (userRepositoryPort.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
        }
    }

    private void ensureEmailIsAvailable(String email) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(email);
        }
    }
}
