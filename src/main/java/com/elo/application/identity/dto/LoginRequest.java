package com.elo.application.identity.dto;

import com.elo.application.identity.command.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
