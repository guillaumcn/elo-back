package com.elo.infrastructure.adapter.in.web.identity.mapper;

import com.elo.application.identity.dto.UserResponse;
import com.elo.domain.identity.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseMapperTest {

    @Test
    void shouldMapUserToResponse() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-15T10:30:00Z");
        User user = User.builder()
                .id(id)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hashed")
                .avatarUrl("https://example.com/avatar.png")
                .bio("Hello world")
                .deleted(false)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        UserResponse response = UserResponseMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.avatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.bio()).isEqualTo("Hello world");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapUserWithNullOptionalFields() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("bob")
                .email("bob@example.com")
                .passwordHash("hashed")
                .createdAt(Instant.now())
                .build();

        UserResponse response = UserResponseMapper.toResponse(user);

        assertThat(response.avatarUrl()).isNull();
        assertThat(response.bio()).isNull();
    }

    @Test
    void shouldNotExposePasswordHash() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .passwordHash("secret-hash")
                .createdAt(Instant.now())
                .build();

        UserResponse response = UserResponseMapper.toResponse(user);

        assertThat(response.toString()).doesNotContain("secret-hash");
    }
}
