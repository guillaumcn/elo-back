package com.elo.infrastructure.adapter.in.web.identity.mapper;

import com.elo.application.identity.dto.PublicUserResponse;
import com.elo.application.identity.dto.UserResponse;
import com.elo.domain.identity.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static PublicUserResponse toPublicResponse(User user) {
        return new PublicUserResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
