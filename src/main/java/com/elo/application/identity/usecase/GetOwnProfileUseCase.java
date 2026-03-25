package com.elo.application.identity.usecase;

import com.elo.application.identity.port.in.GetOwnProfilePort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.UserNotFoundException;
import com.elo.domain.identity.model.User;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetOwnProfileUseCase implements GetOwnProfilePort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User execute(UUID userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }
}
