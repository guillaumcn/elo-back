package com.elo.infrastructure.adapter.in.web.group;

import com.elo.application.group.command.ArchiveGroupCommand;
import com.elo.application.group.command.GetGroupCommand;
import com.elo.application.group.command.JoinGroupCommand;
import com.elo.application.group.command.ListJoinRequestsCommand;
import com.elo.application.group.command.SubmitJoinRequestCommand;
import com.elo.application.group.command.UnarchiveGroupCommand;
import com.elo.application.group.dto.CreateGroupInvitationRequest;
import com.elo.application.group.dto.CreateGroupRequest;
import com.elo.application.group.dto.GroupInvitationResponse;
import com.elo.application.group.dto.GroupJoinRequestResponse;
import com.elo.application.group.dto.GroupMemberResponse;
import com.elo.application.group.dto.GroupResponse;
import com.elo.application.group.dto.HandleJoinRequestRequest;
import com.elo.application.group.dto.JoinByInvitationRequest;
import com.elo.application.group.dto.UpdateGroupRequest;
import com.elo.application.group.port.in.ArchiveGroupPort;
import com.elo.application.group.port.in.CreateGroupInvitationPort;
import com.elo.application.group.port.in.CreateGroupPort;
import com.elo.application.group.port.in.GetGroupPort;
import com.elo.application.group.port.in.HandleJoinRequestPort;
import com.elo.application.group.port.in.JoinGroupByInvitationPort;
import com.elo.application.group.port.in.JoinGroupPort;
import com.elo.application.group.port.in.ListGroupsPort;
import com.elo.application.group.port.in.ListJoinRequestsPort;
import com.elo.application.group.port.in.SubmitJoinRequestPort;
import com.elo.application.group.port.in.UnarchiveGroupPort;
import com.elo.application.group.port.in.UpdateGroupPort;
import com.elo.application.shared.PagedResult;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupInvitation;
import com.elo.domain.group.model.GroupJoinRequest;
import com.elo.domain.group.model.GroupMember;
import com.elo.infrastructure.adapter.in.web.group.mapper.GroupInvitationResponseMapper;
import com.elo.infrastructure.adapter.in.web.group.mapper.GroupJoinRequestResponseMapper;
import com.elo.infrastructure.adapter.in.web.group.mapper.GroupMemberResponseMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    private final JoinGroupPort joinGroupPort;
    private final CreateGroupInvitationPort createGroupInvitationPort;
    private final JoinGroupByInvitationPort joinGroupByInvitationPort;
    private final SubmitJoinRequestPort submitJoinRequestPort;
    private final ListJoinRequestsPort listJoinRequestsPort;
    private final HandleJoinRequestPort handleJoinRequestPort;

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
    public PagedResult<GroupResponse> listGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        PagedResult<Group> result = listGroupsPort.execute(userId, page, size);
        List<GroupResponse> content = result.content().stream()
                .map(GroupResponseMapper::toResponse)
                .toList();
        return new PagedResult<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
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

    @Operation(summary = "Join an open group")
    @ApiResponse(responseCode = "200", description = "Joined group successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Already a member",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Group is not open or is archived",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/join")
    @ResponseStatus(HttpStatus.OK)
    public GroupMemberResponse joinGroup(@PathVariable UUID groupId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        GroupMember member = joinGroupPort.execute(new JoinGroupCommand(groupId, userId));
        return GroupMemberResponseMapper.toResponse(member);
    }

    @Operation(summary = "Create a group invitation")
    @ApiResponse(responseCode = "201", description = "Invitation created successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found or not a member",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupInvitationResponse createInvitation(@PathVariable UUID groupId,
                                                     @Valid @RequestBody(required = false) CreateGroupInvitationRequest request,
                                                     Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        CreateGroupInvitationRequest effectiveRequest = request != null ? request : new CreateGroupInvitationRequest(null);
        GroupInvitation invitation = createGroupInvitationPort.execute(effectiveRequest.toCommand(groupId, userId));
        return GroupInvitationResponseMapper.toResponse(invitation);
    }

    @Operation(summary = "Join a group by invitation token")
    @ApiResponse(responseCode = "200", description = "Joined group successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Invitation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Already a member",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Invitation expired or group is archived",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/join-by-invitation")
    @ResponseStatus(HttpStatus.OK)
    public GroupMemberResponse joinGroupByInvitation(@PathVariable UUID groupId,
                                                      @Valid @RequestBody JoinByInvitationRequest request,
                                                      Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        GroupMember member = joinGroupByInvitationPort.execute(request.toCommand(groupId, userId));
        return GroupMemberResponseMapper.toResponse(member);
    }

    @Operation(summary = "Submit a join request")
    @ApiResponse(responseCode = "201", description = "Join request submitted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Already a member or duplicate pending request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Group is not REQUEST policy or is archived",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/join-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupJoinRequestResponse submitJoinRequest(@PathVariable UUID groupId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        GroupJoinRequest joinRequest = submitJoinRequestPort.execute(new SubmitJoinRequestCommand(groupId, userId));
        return GroupJoinRequestResponseMapper.toResponse(joinRequest);
    }

    @Operation(summary = "List join requests for a group")
    @ApiResponse(responseCode = "200", description = "Join requests retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not an admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{groupId}/join-requests")
    public PagedResult<GroupJoinRequestResponse> listJoinRequests(@PathVariable UUID groupId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        PagedResult<GroupJoinRequest> result = listJoinRequestsPort.execute(new ListJoinRequestsCommand(groupId, userId, page, size));
        List<GroupJoinRequestResponse> content = result.content().stream()
                .map(GroupJoinRequestResponseMapper::toResponse)
                .toList();
        return new PagedResult<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @Operation(summary = "Approve or deny a join request")
    @ApiResponse(responseCode = "200", description = "Join request handled successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not an admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Group or join request not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Join request already resolved",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{groupId}/join-requests/{requestId}")
    public GroupJoinRequestResponse handleJoinRequest(@PathVariable UUID groupId,
                                                       @PathVariable UUID requestId,
                                                       @Valid @RequestBody HandleJoinRequestRequest request,
                                                       Authentication authentication) {
        UUID adminId = (UUID) authentication.getPrincipal();
        GroupJoinRequest joinRequest = handleJoinRequestPort.execute(request.toCommand(groupId, requestId, adminId));
        return GroupJoinRequestResponseMapper.toResponse(joinRequest);
    }
}
