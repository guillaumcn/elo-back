package com.elo.infrastructure.adapter.in.web.identity;

import com.elo.application.identity.command.GetPublicProfileCommand;
import com.elo.application.identity.dto.PublicUserResponse;
import com.elo.application.identity.dto.UpdateProfileRequest;
import com.elo.application.identity.dto.UserResponse;
import com.elo.application.identity.port.in.DeleteAccountPort;
import com.elo.application.identity.port.in.GetOwnProfilePort;
import com.elo.application.identity.port.in.GetPublicProfilePort;
import com.elo.application.identity.port.in.UpdateProfilePort;
import com.elo.domain.identity.model.User;
import com.elo.infrastructure.adapter.in.web.identity.mapper.UserResponseMapper;
import com.elo.infrastructure.configuration.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetOwnProfilePort getOwnProfilePort;
    private final UpdateProfilePort updateProfilePort;
    private final DeleteAccountPort deleteAccountPort;
    private final GetPublicProfilePort getPublicProfilePort;

    @Operation(summary = "Get own profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/me")
    public UserResponse getOwnProfile(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = getOwnProfilePort.execute(userId);
        return UserResponseMapper.toResponse(user);
    }

    @Operation(summary = "Update own profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username already taken",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                      Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = updateProfilePort.execute(request.toCommand(userId));
        return UserResponseMapper.toResponse(user);
    }

    @Operation(summary = "Delete own account")
    @ApiResponse(responseCode = "204", description = "Account deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        deleteAccountPort.execute(userId);
    }

    @Operation(summary = "Get a user's public profile")
    @ApiResponse(responseCode = "200", description = "Public profile retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found or not visible",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{username}")
    public PublicUserResponse getPublicProfile(@PathVariable String username,
                                               Authentication authentication) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        User user = getPublicProfilePort.execute(new GetPublicProfileCommand(requesterId, username));
        return UserResponseMapper.toPublicResponse(user);
    }
}
