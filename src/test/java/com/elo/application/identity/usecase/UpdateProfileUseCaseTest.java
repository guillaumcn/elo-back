package com.elo.application.identity.usecase;

import com.elo.application.identity.command.UpdateProfileCommand;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.UsernameAlreadyTakenException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private UpdateProfileUseCase updateProfileUseCase;

    @BeforeEach
    void setUp() {
        updateProfileUseCase = new UpdateProfileUseCase(userRepositoryPort);
    }

    @Test
    void shouldUpdateUsername() {
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepositoryPort.existsByUsername("alice_new")).thenReturn(false);
        when(userRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = updateProfileUseCase.execute(
                new UpdateProfileCommand(user.getId(), "alice_new", null, null));

        assertThat(result.getUsername()).isEqualTo("alice_new");
    }

    @Test
    void shouldUpdateBioAndAvatar() {
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = updateProfileUseCase.execute(
                new UpdateProfileCommand(user.getId(), null, "https://avatar.url", "Hello!"));

        assertThat(result.getAvatarUrl()).isEqualTo("https://avatar.url");
        assertThat(result.getBio()).isEqualTo("Hello!");
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void shouldThrowWhenNewUsernameIsTaken() {
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepositoryPort.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> updateProfileUseCase.execute(
                new UpdateProfileCommand(user.getId(), "bob", null, null)))
                .isInstanceOf(UsernameAlreadyTakenException.class);
    }

    @Test
    void shouldAllowKeepingSameUsername() {
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = updateProfileUseCase.execute(
                new UpdateProfileCommand(user.getId(), "alice", null, "New bio"));

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getBio()).isEqualTo("New bio");
    }
}
