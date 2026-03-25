package com.elo.application.identity.command;

public record LoginCommand(String email, String rawPassword) {
}
