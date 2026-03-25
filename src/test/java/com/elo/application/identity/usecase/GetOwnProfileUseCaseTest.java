package com.elo.application.identity.usecase;

import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.UserNotFoundException;
import com.elo.domain.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOwnProfileUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private GetOwnProfileUseCase getOwnProfileUseCase;

    @BeforeEach
    void setUp() {
        getOwnProfileUseCase = new GetOwnProfileUseCase(userRepositoryPort);
    }

    @Test
    void shouldReturnUserById() {
        UUID userId = UUID.randomUUID();
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        User result = getOwnProfileUseCase.execute(userId);

        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getOwnProfileUseCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
