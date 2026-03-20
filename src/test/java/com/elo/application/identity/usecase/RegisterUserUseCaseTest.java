package com.elo.application.identity.usecase;

import com.elo.application.identity.command.RegisterUserCommand;
import com.elo.application.identity.port.out.PasswordHasherPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.exception.EmailAlreadyTakenException;
import com.elo.domain.identity.exception.UsernameAlreadyTakenException;
import com.elo.domain.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasher;

    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(userRepositoryPort, passwordHasher);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepositoryPort.existsByUsername("alice")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordHasher.hash("Str0ngP@ss!")).thenReturn("hashed-password");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new RegisterUserCommand("alice", "alice@example.com", "Str0ngP@ss!");
        User user = registerUserUseCase.execute(command);

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.getId()).isNotNull();
        assertThat(user.isDeleted()).isFalse();

        verify(userRepositoryPort).save(any(User.class));
        verify(passwordHasher).hash("Str0ngP@ss!");
    }

    @Test
    void shouldThrowWhenUsernameAlreadyTaken() {
        when(userRepositoryPort.existsByUsername("alice")).thenReturn(true);

        var command = new RegisterUserCommand("alice", "alice@example.com", "Str0ngP@ss!");

        assertThatThrownBy(() -> registerUserUseCase.execute(command))
                .isInstanceOf(UsernameAlreadyTakenException.class)
                .hasMessageContaining("alice");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenEmailAlreadyTaken() {
        when(userRepositoryPort.existsByUsername("bob")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("alice@example.com")).thenReturn(true);

        var command = new RegisterUserCommand("bob", "alice@example.com", "Str0ngP@ss!");

        assertThatThrownBy(() -> registerUserUseCase.execute(command))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessageContaining("alice@example.com");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void shouldHashPasswordBeforeSaving() {
        when(userRepositoryPort.existsByUsername("alice")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordHasher.hash("mypassword")).thenReturn("$2a$10$hashedvalue");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new RegisterUserCommand("alice", "alice@example.com", "mypassword");
        registerUserUseCase.execute(command);

        verify(passwordHasher).hash(eq("mypassword"));
    }

    @Test
    void shouldNotCheckEmailIfUsernameAlreadyTaken() {
        when(userRepositoryPort.existsByUsername("alice")).thenReturn(true);

        var command = new RegisterUserCommand("alice", "alice@example.com", "Str0ngP@ss!");

        assertThatThrownBy(() -> registerUserUseCase.execute(command))
                .isInstanceOf(UsernameAlreadyTakenException.class);

        verify(userRepositoryPort, never()).existsByEmail(any());
    }
}
