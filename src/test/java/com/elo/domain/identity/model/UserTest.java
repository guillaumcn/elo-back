package com.elo.domain.identity.model;

import com.elo.domain.identity.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        User user = User.create("alice", "alice@example.com", "hashed-password");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getBio()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void shouldRejectBlankUsername(String username) {
        assertThatThrownBy(() -> User.create(username, "alice@example.com", "hashed"))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("Username is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a"})
    void shouldRejectUsernameShorterThan3Characters(String username) {
        assertThatThrownBy(() -> User.create(username, "alice@example.com", "hashed"))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("between 3 and 50");
    }

    @Test
    void shouldRejectUsernameLongerThan50Characters() {
        String longUsername = "a".repeat(51);

        assertThatThrownBy(() -> User.create(longUsername, "alice@example.com", "hashed"))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("between 3 and 50");
    }

    @Test
    void shouldAcceptUsernameAtBoundaryLengths() {
        assertThat(User.create("abc", "a@b.c", "hashed").getUsername()).hasSize(3);
        assertThat(User.create("a".repeat(50), "a@b.c", "hashed").getUsername()).hasSize(50);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void shouldRejectBlankEmail(String email) {
        assertThatThrownBy(() -> User.create("alice", email, "hashed"))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("Email is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "missing@dot", "@no-local.com", "no-at-sign.com", "spaces in@email.com"})
    void shouldRejectInvalidEmailFormat(String email) {
        assertThatThrownBy(() -> User.create("alice", email, "hashed"))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("Email must be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@domain.com", "a@b.c", "user+tag@domain.co.uk"})
    void shouldAcceptValidEmailFormats(String email) {
        User user = User.create("alice", email, "hashed");
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldRejectNullPasswordHash() {
        assertThatThrownBy(() -> User.create("alice", "alice@example.com", null))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("Password hash is required");
    }

    @Test
    void shouldRejectEmptyPasswordHash() {
        assertThatThrownBy(() -> User.create("alice", "alice@example.com", ""))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("Password hash is required");
    }

    @Test
    void shouldGenerateUniqueIdsForDifferentUsers() {
        User user1 = User.create("alice", "alice@example.com", "hashed");
        User user2 = User.create("bob", "bob@example.com", "hashed");

        assertThat(user1.getId()).isNotEqualTo(user2.getId());
    }

    @Test
    void shouldUpdateProfileBioWhenWithinLimit() {
        User user = User.create("alice", "alice@example.com", "hashed");
        String validBio = "a".repeat(500);
        user.updateProfile(null, null, validBio);

        assertThat(user.getBio()).isEqualTo(validBio);
    }

    @Test
    void shouldRejectBioExceeding500Characters() {
        User user = User.create("alice", "alice@example.com", "hashed");
        String tooLongBio = "a".repeat(501);

        assertThatThrownBy(() -> user.updateProfile(null, null, tooLongBio))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining("Bio must be at most 500 characters");
    }
}
