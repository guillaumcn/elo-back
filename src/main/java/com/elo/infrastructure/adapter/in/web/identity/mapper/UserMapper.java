package com.elo.infrastructure.adapter.in.web.identity.mapper;

import com.elo.application.identity.command.RegisterUserCommand;
import com.elo.application.identity.dto.RegisterRequest;
import com.elo.application.identity.dto.UserResponse;
import com.elo.domain.identity.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static RegisterUserCommand toCommand(RegisterRequest request) {
        return new RegisterUserCommand(request.username(), request.email(), request.password());
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt()
        );
    }
}
