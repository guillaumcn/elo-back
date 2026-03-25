package com.elo.application.identity.usecase;

import com.elo.application.identity.command.LoginCommand;
import com.elo.application.identity.port.in.LoginUserPort;
import com.elo.application.identity.port.out.PasswordHasherPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.InvalidCredentialsException;
import com.elo.domain.identity.model.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginUserUseCase implements LoginUserPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasher;

    @Override
    public User execute(LoginCommand command) {
        User user = findActiveUserByEmail(command.email());
        ensurePasswordMatches(command.rawPassword(), user.getPasswordHash());
        return user;
    }

    private User findActiveUserByEmail(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (user.isDeleted()) throw new InvalidCredentialsException();
        return user;
    }

    private void ensurePasswordMatches(String rawPassword, String passwordHash) {
        if (!passwordHasher.matches(rawPassword, passwordHash)) {
            throw new InvalidCredentialsException();
        }
    }
}
