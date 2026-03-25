package com.elo.application.identity.command;

public record RegisterUserCommand(String username, String email, String rawPassword) {
}
