package com.elo.application.identity.usecase;

import com.elo.application.identity.port.in.DeleteAccountPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.UserNotFoundException;
import com.elo.domain.identity.model.User;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteAccountUseCase implements DeleteAccountPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public void execute(UUID userId) {
        User user = findUserById(userId);
        user.deleteAccount();
        userRepositoryPort.save(user);
    }

    private User findUserById(UUID userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }
}
