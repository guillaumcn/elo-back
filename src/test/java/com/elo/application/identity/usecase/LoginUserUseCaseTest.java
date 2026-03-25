package com.elo.application.identity.usecase;

import com.elo.application.identity.command.LoginCommand;
import com.elo.application.identity.port.out.PasswordHasherPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.InvalidCredentialsException;
import com.elo.domain.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasher;

    private LoginUserUseCase loginUserUseCase;

    @BeforeEach
    void setUp() {
        loginUserUseCase = new LoginUserUseCase(userRepositoryPort, passwordHasher);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = User.create("alice", "alice@example.com", "hashed-password");
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Str0ngP@ss!", "hashed-password")).thenReturn(true);

        User result = loginUserUseCase.execute(new LoginCommand("alice@example.com", "Str0ngP@ss!"));

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(userRepositoryPort.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUserUseCase.execute(new LoginCommand("unknown@example.com", "Str0ngP@ss!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        User user = User.create("alice", "alice@example.com", "hashed-password");
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("WrongPassword", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> loginUserUseCase.execute(new LoginCommand("alice@example.com", "WrongPassword")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowWhenUserIsDeleted() {
        User user = User.create("alice", "alice@example.com", "hashed-password");
        User deletedUser = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .deleted(true)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> loginUserUseCase.execute(new LoginCommand("alice@example.com", "Str0ngP@ss!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldUseGenericErrorMessageToAvoidUserEnumeration() {
        when(userRepositoryPort.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUserUseCase.execute(new LoginCommand("unknown@example.com", "Str0ngP@ss!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
