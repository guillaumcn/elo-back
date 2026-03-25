package com.elo.domain.identity.model;

import com.elo.domain.identity.exception.InvalidUserException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Getter
public class User {

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public User(UUID id, String username, String email, String passwordHash,
                String avatarUrl, String bio, boolean deleted,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int PASSWORD_HASH_MIN_LENGTH = 1;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void updateProfile(String username, String avatarUrl, String bio) {
        if (username != null) {
            validateUsername(username);
            this.username = username;
        }
        if (avatarUrl != null) this.avatarUrl = avatarUrl;
        if (bio != null) this.bio = bio;
        this.updatedAt = Instant.now();
    }

    public void deleteAccount() {
        String anonymizedUsername = "deleted_" + id;
        this.username = anonymizedUsername;
        this.email = anonymizedUsername + "@deleted.invalid";
        this.avatarUrl = null;
        this.bio = null;
        this.deleted = true;
        this.updatedAt = Instant.now();
    }

    public static User create(String username, String email, String passwordHash) {
        validateUsername(username);
        validateEmail(email);
        validatePasswordHash(passwordHash);

        Instant now = Instant.now();
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserException("Username is required");
        }
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            throw new InvalidUserException(
                    "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidUserException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserException("Email must be valid");
        }
    }

    private static void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.length() < PASSWORD_HASH_MIN_LENGTH) {
            throw new InvalidUserException("Password hash is required");
        }
    }
}
