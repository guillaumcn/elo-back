package com.elo.application.identity.usecase;

import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.domain.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private DeleteAccountUseCase deleteAccountUseCase;

    @BeforeEach
    void setUp() {
        deleteAccountUseCase = new DeleteAccountUseCase(userRepositoryPort);
    }

    @Test
    void shouldSoftDeleteAndAnonymizeUser() {
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));

        deleteAccountUseCase.execute(user.getId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.isDeleted()).isTrue();
        assertThat(saved.getUsername()).startsWith("deleted_");
        assertThat(saved.getAvatarUrl()).isNull();
        assertThat(saved.getBio()).isNull();
        assertThat(saved.getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldPreserveUserIdAfterDeletion() {
        User user = User.create("alice", "alice@example.com", "hashed");
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));

        deleteAccountUseCase.execute(user.getId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(user.getId());
    }
}
