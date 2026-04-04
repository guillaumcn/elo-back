package com.elo.infrastructure.adapter.in.web.group;

import com.elo.application.group.command.ArchiveGroupCommand;
import com.elo.application.group.command.GetGroupCommand;
import com.elo.application.group.command.UnarchiveGroupCommand;
import com.elo.application.group.dto.CreateGroupRequest;
import com.elo.application.group.dto.GroupResponse;
import com.elo.application.group.dto.UpdateGroupRequest;
import com.elo.application.group.port.in.ArchiveGroupPort;
import com.elo.application.group.port.in.CreateGroupPort;
import com.elo.application.group.port.in.GetGroupPort;
import com.elo.application.group.port.in.ListGroupsPort;
import com.elo.application.group.port.in.UnarchiveGroupPort;
import com.elo.application.group.port.in.UpdateGroupPort;
import com.elo.domain.group.model.Group;
import com.elo.infrastructure.adapter.in.web.group.mapper.GroupResponseMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@Tag(name = "Groups", description = "Group management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final CreateGroupPort createGroupPort;
    private final GetGroupPort getGroupPort;
    private final UpdateGroupPort updateGroupPort;
    private final ListGroupsPort listGroupsPort;
    private final ArchiveGroupPort archiveGroupPort;
    private final UnarchiveGroupPort unarchiveGroupPort;

    @Operation(summary = "Create a group")
    @ApiResponse(responseCode = "201", description = "Group created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@Valid @RequestBody CreateGroupRequest request,
                                     Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Group group = createGroupPort.execute(request.toCommand(userId));
        return GroupResponseMapper.toResponse(group);
    }

    @Operation(summary = "List my groups")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public List<GroupResponse> listGroups(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return listGroupsPort.execute(userId).stream()
                .map(GroupResponseMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Get group details")
    @ApiResponse(responseCode = "200", description = "Group retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found or not a member",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{groupId}")
    public GroupResponse getGroup(@PathVariable UUID groupId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Group group = getGroupPort.execute(new GetGroupCommand(groupId, userId));
        return GroupResponseMapper.toResponse(group);
    }

    @Operation(summary = "Archive a group")
    @ApiResponse(responseCode = "200", description = "Group archived successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not an admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Group is already archived",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/archive")
    @ResponseStatus(HttpStatus.OK)
    public GroupResponse archiveGroup(@PathVariable UUID groupId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Group group = archiveGroupPort.execute(new ArchiveGroupCommand(groupId, userId));
        return GroupResponseMapper.toResponse(group);
    }

    @Operation(summary = "Unarchive a group")
    @ApiResponse(responseCode = "200", description = "Group unarchived successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not an admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Group is not archived",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/unarchive")
    @ResponseStatus(HttpStatus.OK)
    public GroupResponse unarchiveGroup(@PathVariable UUID groupId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Group group = unarchiveGroupPort.execute(new UnarchiveGroupCommand(groupId, userId));
        return GroupResponseMapper.toResponse(group);
    }

    @Operation(summary = "Update group details")
    @ApiResponse(responseCode = "200", description = "Group updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not an admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{groupId}")
    public GroupResponse updateGroup(@PathVariable UUID groupId,
                                     @Valid @RequestBody UpdateGroupRequest request,
                                     Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Group group = updateGroupPort.execute(request.toCommand(groupId, userId));
        return GroupResponseMapper.toResponse(group);
    }
}
