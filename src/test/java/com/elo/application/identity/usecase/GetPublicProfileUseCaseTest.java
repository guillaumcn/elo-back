package com.elo.application.identity.usecase;

import com.elo.application.identity.command.GetPublicProfileCommand;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.application.identity.port.out.UserVisibilityPort;
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
class GetPublicProfileUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserVisibilityPort userVisibilityPort;

    private GetPublicProfileUseCase getPublicProfileUseCase;

    @BeforeEach
    void setUp() {
        getPublicProfileUseCase = new GetPublicProfileUseCase(userRepositoryPort, userVisibilityPort);
    }

    @Test
    void shouldReturnPublicProfileWhenVisible() {
        UUID requesterId = UUID.randomUUID();
        User target = User.create("bob", "bob@example.com", "hashed");
        when(userRepositoryPort.findByUsername("bob")).thenReturn(Optional.of(target));
        when(userVisibilityPort.isVisible(requesterId, target.getId())).thenReturn(true);

        User result = getPublicProfileUseCase.execute(new GetPublicProfileCommand(requesterId, "bob"));

        assertThat(result.getUsername()).isEqualTo("bob");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID requesterId = UUID.randomUUID();
        when(userRepositoryPort.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPublicProfileUseCase.execute(
                new GetPublicProfileCommand(requesterId, "unknown")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowWhenNotVisible() {
        UUID requesterId = UUID.randomUUID();
        User target = User.create("charlie", "charlie@example.com", "hashed");
        when(userRepositoryPort.findByUsername("charlie")).thenReturn(Optional.of(target));
        when(userVisibilityPort.isVisible(requesterId, target.getId())).thenReturn(false);

        assertThatThrownBy(() -> getPublicProfileUseCase.execute(
                new GetPublicProfileCommand(requesterId, "charlie")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowWhenUserIsDeleted() {
        UUID requesterId = UUID.randomUUID();
        User target = User.create("alice", "alice@example.com", "hashed");
        target.deleteAccount();
        when(userRepositoryPort.findByUsername(target.getUsername())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> getPublicProfileUseCase.execute(
                new GetPublicProfileCommand(requesterId, target.getUsername())))
                .isInstanceOf(UserNotFoundException.class);
    }
}
