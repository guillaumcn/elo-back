package com.elo.application.identity.command;

import com.elo.domain.identity.model.User;

public record RegisterUserCommand(String username, String email, String rawPassword) {

    public User mapToDomain(String hashedPassword) {
        return User.create(username, email, hashedPassword);
    }
}
