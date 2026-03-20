package com.elo.application.identity.command;

import com.elo.domain.identity.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterUserCommandTest {

    @Test
    void shouldMapToDomainUser() {
        var command = new RegisterUserCommand("alice", "alice@example.com", "rawPassword");

        User user = command.mapToDomain("hashed-password");

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.getId()).isNotNull();
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    void shouldUseHashedPasswordNotRawPassword() {
        var command = new RegisterUserCommand("alice", "alice@example.com", "rawPassword");

        User user = command.mapToDomain("$2a$10$hashedvalue");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$hashedvalue");
        assertThat(user.getPasswordHash()).isNotEqualTo("rawPassword");
    }
}
