package com.elo.acceptance.group;

import com.elo.acceptance.ScenarioContext;
import com.elo.application.group.dto.CreateGroupInvitationRequest;
import com.elo.application.group.dto.CreateGroupRequest;
import com.elo.application.group.dto.JoinByInvitationRequest;
import com.elo.application.group.dto.UpdateGroupRequest;
import com.elo.application.identity.dto.LoginRequest;
import com.elo.application.identity.dto.RegisterRequest;
import com.elo.domain.group.model.JoinPolicy;
import com.elo.domain.group.model.MemberRole;
import com.elo.infrastructure.adapter.out.persistence.group.GroupMemberJpaEntity;
import com.elo.infrastructure.adapter.out.persistence.group.GroupMemberJpaRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private GroupMemberJpaRepository groupMemberJpaRepository;

    private String pendingName;
    private String pendingDescription;
    private JoinPolicy pendingJoinPolicy;

    private String pendingUpdateName;
    private String pendingUpdateDescription;
    private JoinPolicy pendingUpdateJoinPolicy;

    private UUID currentGroupId;
    private String lastOtherUserToken;
    private String lastInvitationToken;
    private String pendingInvitationToken;
    private String pendingInvitationExpiresAt;
    private UUID lastJoinRequestId;
    private UUID lastJoinRequestUserId;
    private List<Map<String, Object>> lastJoinRequestsList;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    // ── Create group request builder steps ──────────────────────────────────

    @Given("a create group request with name {string}")
    public void aCreateGroupRequestWithName(String name) {
        pendingName = name;
        pendingDescription = null;
        pendingJoinPolicy = null;
    }

    @And("the create group description is {string}")
    public void theCreateGroupDescriptionIs(String description) {
        pendingDescription = description;
    }

    @And("the create group join policy is {string}")
    public void theCreateGroupJoinPolicyIs(String joinPolicy) {
        pendingJoinPolicy = JoinPolicy.valueOf(joinPolicy);
    }

    @When("I submit the create group request")
    public void iSubmitTheCreateGroupRequest() {
        CreateGroupRequest request = new CreateGroupRequest(pendingName, pendingDescription, pendingJoinPolicy);
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                authorizedEntity(request), Map.class));
        extractGroupId();
    }

    // ── Update group request builder steps ──────────────────────────────────

    @And("an update group request with name {string}")
    public void anUpdateGroupRequestWithName(String name) {
        pendingUpdateName = name;
        pendingUpdateDescription = null;
        pendingUpdateJoinPolicy = null;
    }

    @And("an update group request with join policy {string}")
    public void anUpdateGroupRequestWithJoinPolicy(String joinPolicy) {
        pendingUpdateName = null;
        pendingUpdateDescription = null;
        pendingUpdateJoinPolicy = JoinPolicy.valueOf(joinPolicy);
    }

    @And("an update group request with description {string}")
    public void anUpdateGroupRequestWithDescription(String description) {
        pendingUpdateName = null;
        pendingUpdateDescription = description;
        pendingUpdateJoinPolicy = null;
    }

    @When("I submit the update group request")
    public void iSubmitTheUpdateGroupRequest() {
        UpdateGroupRequest request = new UpdateGroupRequest(pendingUpdateName, pendingUpdateDescription, pendingUpdateJoinPolicy);
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId, HttpMethod.PUT,
                authorizedEntity(request), Map.class));
    }

    @When("I submit the update group request without authentication")
    public void iSubmitTheUpdateGroupRequestWithoutAuthentication() {
        UpdateGroupRequest request = new UpdateGroupRequest(pendingUpdateName, pendingUpdateDescription, pendingUpdateJoinPolicy);
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId, HttpMethod.PUT,
                unauthenticatedEntity(request), Map.class));
    }

    @When("I archive the group without authentication")
    public void iArchiveTheGroupWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/archive", HttpMethod.POST,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I unarchive the group without authentication")
    public void iUnarchiveTheGroupWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/unarchive", HttpMethod.POST,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I submit the create group request without authentication")
    public void iSubmitTheCreateGroupRequestWithoutAuthentication() {
        CreateGroupRequest request = new CreateGroupRequest(pendingName, pendingDescription, pendingJoinPolicy);
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                unauthenticatedEntity(request), Map.class));
    }

    @When("I try to list my groups without authentication")
    public void iTryToListMyGroupsWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.GET,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I request the group details without authentication")
    public void iRequestTheGroupDetailsWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId, HttpMethod.GET,
                unauthenticatedEntity(null), Map.class));
    }

    // ── Unauthenticated precondition steps ──────────────────────────────────

    @Given("a non-existent group id")
    public void aNonExistentGroupId() {
        currentGroupId = UUID.randomUUID();
    }

    // ── Precondition steps ───────────────────────────────────────────────────

    @And("I have created a group named {string} with join policy {string}")
    public void iHaveCreatedAGroupNamedWithJoinPolicy(String name, String joinPolicy) {
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                authorizedEntity(request), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
    }

    @And("I have created another group named {string} with join policy {string}")
    public void iHaveCreatedAnotherGroupNamedWithJoinPolicy(String name, String joinPolicy) {
        iHaveCreatedAGroupNamedWithJoinPolicy(name, joinPolicy);
    }

    @And("another user has created a group named {string}")
    public void anotherUserHasCreatedAGroupNamed(String name) {
        String otherToken = registerAndLoginOtherUser();
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.INVITATION);
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
    }

    @And("another user has created a group named {string} with join policy {string} and added me as a member")
    public void anotherUserHasCreatedAGroupAndAddedMeAsMember(String name, String joinPolicy) {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));

        UUID myUserId = getAuthenticatedUserId();
        GroupMemberJpaEntity memberEntity = GroupMemberJpaEntity.builder()
                .id(UUID.randomUUID())
                .groupId(currentGroupId)
                .userId(myUserId)
                .role(MemberRole.MEMBER)
                .joinedAt(Instant.now())
                .build();
        groupMemberJpaRepository.save(memberEntity);
    }

    // ── Query steps ─────────────────────────────────────────────────────────

    @When("I request the group details")
    public void iRequestTheGroupDetails() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId, HttpMethod.GET,
                authorizedEntity(null), Map.class));
    }

    @When("I request my groups")
    public void iRequestMyGroups() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.GET,
                authorizedEntity(null), Map.class));
    }

    // ── Assertion steps ─────────────────────────────────────────────────────

    @And("the group name is {string}")
    public void theGroupNameIs(String expectedName) {
        assertThat(scenarioContext.getResponse().getBody().get("name")).isEqualTo(expectedName);
    }

    @And("the group join policy is {string}")
    public void theGroupJoinPolicyIs(String expectedPolicy) {
        assertThat(scenarioContext.getResponse().getBody().get("joinPolicy")).isEqualTo(expectedPolicy);
    }

    @And("the group member count is {int}")
    public void theGroupMemberCountIs(int expectedCount) {
        assertThat(scenarioContext.getResponse().getBody().get("memberCount")).isEqualTo(expectedCount);
    }

    @And("I am listed as the group creator")
    public void iAmListedAsTheGroupCreator() {
        UUID myUserId = getAuthenticatedUserId();
        String createdBy = (String) scenarioContext.getResponse().getBody().get("createdBy");
        assertThat(UUID.fromString(createdBy)).isEqualTo(myUserId);
    }

    @And("the group list contains {string}")
    public void theGroupListContains(String groupName) {
        List<Map<String, Object>> groups = (List<Map<String, Object>>) scenarioContext.getResponse().getBody().get("content");
        assertThat(groups).isNotNull();
        assertThat(groups).anyMatch(g -> groupName.equals(g.get("name")));
    }

    @And("the group list is empty")
    public void theGroupListIsEmpty() {
        List<?> groups = (List<?>) scenarioContext.getResponse().getBody().get("content");
        assertThat(groups).isNotNull().isEmpty();
    }

    @And("the group description is {string}")
    public void theGroupDescriptionIs(String expectedDescription) {
        assertThat(scenarioContext.getResponse().getBody().get("description")).isEqualTo(expectedDescription);
    }

    // ── Archive / Unarchive steps ────────────────────────────────────────────

    @When("I archive the group")
    public void iArchiveTheGroup() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/archive", HttpMethod.POST,
                authorizedEntity(null), Map.class));
    }

    @When("I unarchive the group")
    public void iUnarchiveTheGroup() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/unarchive", HttpMethod.POST,
                authorizedEntity(null), Map.class));
    }

    @And("I have archived the group")
    public void iHaveArchivedTheGroup() {
        restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/archive", HttpMethod.POST,
                authorizedEntity(null), Map.class);
    }

    @And("an admin has archived the group")
    public void anAdminHasArchivedTheGroup() {
        restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/archive", HttpMethod.POST,
                entityWithToken(null, lastOtherUserToken), Map.class);
    }

    @And("the group is archived")
    public void theGroupIsArchived() {
        assertThat(scenarioContext.getResponse().getBody().get("archived")).isEqualTo(true);
    }

    @And("the group is not archived")
    public void theGroupIsNotArchived() {
        assertThat(scenarioContext.getResponse().getBody().get("archived")).isEqualTo(false);
    }

    // ── Join steps ───────────────────────────────────────────────────────────

    @And("another user has created a group named {string} with join policy {string}")
    public void anotherUserHasCreatedAGroupNamedWithJoinPolicy(String name, String joinPolicy) {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
    }

    @And("another user has created a group named {string} with join policy {string} and archived it")
    public void anotherUserHasCreatedAGroupWithJoinPolicyAndArchivedIt(String name, String joinPolicy) {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
        restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/archive", HttpMethod.POST,
                entityWithToken(null, otherToken), Map.class);
    }

    @And("another user has created a group named {string} with join policy {string} and created an invitation")
    public void anotherUserHasCreatedAGroupWithJoinPolicyAndCreatedAnInvitation(String name, String joinPolicy) {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
        var invResponse = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                entityWithToken(null, otherToken), Map.class);
        lastInvitationToken = (String) invResponse.getBody().get("token");
        pendingInvitationToken = lastInvitationToken;
    }

    @And("another user has created a group named {string} with join policy {string} and created an expired invitation")
    public void anotherUserHasCreatedAGroupWithJoinPolicyAndCreatedAnExpiredInvitation(String name, String joinPolicy) {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
        CreateGroupInvitationRequest invRequest = new CreateGroupInvitationRequest(Instant.now().minusSeconds(1));
        var invResponse = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                entityWithToken(invRequest, otherToken), Map.class);
        lastInvitationToken = (String) invResponse.getBody().get("token");
        pendingInvitationToken = lastInvitationToken;
    }

    @And("another user has created a group named {string} with join policy {string} and added me as a member and created an invitation")
    public void anotherUserHasCreatedAGroupAndAddedMeAsMemberAndCreatedAnInvitation(String name, String joinPolicy) {
        anotherUserHasCreatedAGroupAndAddedMeAsMember(name, joinPolicy);
        var invResponse = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                entityWithToken(null, lastOtherUserToken), Map.class);
        lastInvitationToken = (String) invResponse.getBody().get("token");
        pendingInvitationToken = lastInvitationToken;
    }

    @When("I join the group without authentication")
    public void iJoinTheGroupWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join", HttpMethod.POST,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I create an invitation for the group without authentication")
    public void iCreateAnInvitationForTheGroupWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I join the group by invitation without authentication")
    public void iJoinTheGroupByInvitationWithoutAuthentication() {
        JoinByInvitationRequest request = new JoinByInvitationRequest("dummy-token");
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-by-invitation", HttpMethod.POST,
                unauthenticatedEntity(request), Map.class));
    }

    @And("another user has created a group named {string} with join policy {string} and created an invitation and archived it")
    public void anotherUserHasCreatedAGroupWithJoinPolicyAndCreatedInvitationAndArchivedIt(String name, String joinPolicy) {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        CreateGroupRequest request = new CreateGroupRequest(name, null, JoinPolicy.valueOf(joinPolicy));
        var response = restTemplate.exchange(
                baseUrl() + "/groups", HttpMethod.POST,
                entityWithToken(request, otherToken), Map.class);
        currentGroupId = UUID.fromString((String) response.getBody().get("id"));
        var invResponse = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                entityWithToken(null, otherToken), Map.class);
        lastInvitationToken = (String) invResponse.getBody().get("token");
        pendingInvitationToken = lastInvitationToken;
        restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/archive", HttpMethod.POST,
                entityWithToken(null, otherToken), Map.class);
    }

    @When("I join the group")
    public void iJoinTheGroup() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join", HttpMethod.POST,
                authorizedEntity(null), Map.class));
    }

    @When("I create an invitation for the group")
    public void iCreateAnInvitationForTheGroup() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                authorizedEntity(null), Map.class));
        Map body = scenarioContext.getResponse().getBody();
        if (body != null && body.containsKey("token")) {
            lastInvitationToken = (String) body.get("token");
        }
    }

    @When("I create an invitation with expiration {string}")
    public void iCreateAnInvitationWithExpiration(String expiresAt) {
        CreateGroupInvitationRequest request = new CreateGroupInvitationRequest(Instant.parse(expiresAt));
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/invitations", HttpMethod.POST,
                authorizedEntity(request), Map.class));
        Map body = scenarioContext.getResponse().getBody();
        if (body != null && body.containsKey("token")) {
            lastInvitationToken = (String) body.get("token");
        }
    }

    @When("I join the group by invitation")
    public void iJoinTheGroupByInvitation() {
        JoinByInvitationRequest request = new JoinByInvitationRequest(pendingInvitationToken);
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-by-invitation", HttpMethod.POST,
                authorizedEntity(request), Map.class));
    }

    @When("I join the group with token {string}")
    public void iJoinTheGroupWithToken(String token) {
        JoinByInvitationRequest request = new JoinByInvitationRequest(token);
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-by-invitation", HttpMethod.POST,
                authorizedEntity(request), Map.class));
    }

    @Then("I am now a member of the group")
    public void iAmNowAMemberOfTheGroup() {
        assertThat(scenarioContext.getResponse().getBody().get("role")).isEqualTo("MEMBER");
    }

    @Then("the invitation has a token")
    public void theInvitationHasAToken() {
        assertThat(scenarioContext.getResponse().getBody().get("token")).isNotNull().isInstanceOf(String.class);
    }

    @And("the invitation expiry is null")
    public void theInvitationExpiryIsNull() {
        assertThat(scenarioContext.getResponse().getBody()).containsKey("expiresAt");
        assertThat(scenarioContext.getResponse().getBody().get("expiresAt")).isNull();
    }

    // ── Join request steps ───────────────────────────────────────────────────

    @When("I submit a join request for the group")
    public void iSubmitAJoinRequestForTheGroup() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests", HttpMethod.POST,
                authorizedEntity(null), Map.class));
        Map body = scenarioContext.getResponse().getBody();
        if (body != null && body.containsKey("id")) {
            lastJoinRequestId = UUID.fromString((String) body.get("id"));
        }
    }

    @And("I have submitted a join request for the group")
    public void iHaveSubmittedAJoinRequestForTheGroup() {
        var response = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests", HttpMethod.POST,
                authorizedEntity(null), Map.class);
        lastJoinRequestId = UUID.fromString((String) response.getBody().get("id"));
    }

    @And("another user has submitted a join request for the group")
    public void anotherUserHasSubmittedAJoinRequestForTheGroup() {
        String otherToken = registerAndLoginOtherUser();
        lastOtherUserToken = otherToken;
        var response = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests", HttpMethod.POST,
                entityWithToken(null, otherToken), Map.class);
        lastJoinRequestId = UUID.fromString((String) response.getBody().get("id"));
        lastJoinRequestUserId = UUID.fromString((String) response.getBody().get("userId"));
    }

    @When("I approve the join request")
    public void iApproveTheJoinRequest() {
        Map<String, String> body = Map.of("action", "APPROVE");
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests/" + lastJoinRequestId, HttpMethod.PUT,
                authorizedEntity(body), Map.class));
    }

    @When("I deny the join request")
    public void iDenyTheJoinRequest() {
        Map<String, String> body = Map.of("action", "DENY");
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests/" + lastJoinRequestId, HttpMethod.PUT,
                authorizedEntity(body), Map.class));
    }

    @And("I have approved the join request")
    public void iHaveApprovedTheJoinRequest() {
        Map<String, String> body = Map.of("action", "APPROVE");
        restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests/" + lastJoinRequestId, HttpMethod.PUT,
                authorizedEntity(body), Map.class);
    }

    @When("I list join requests for the group")
    public void iListJoinRequestsForTheGroup() {
        ResponseEntity<Map> raw = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests", HttpMethod.GET,
                authorizedEntity(null), Map.class);
        scenarioContext.setResponse(raw);
        Map body = raw.getBody();
        if (body != null && body.get("content") instanceof List<?>) {
            lastJoinRequestsList = (List<Map<String, Object>>) body.get("content");
        } else {
            lastJoinRequestsList = null;
        }
    }

    @And("the join request status is {string}")
    public void theJoinRequestStatusIs(String expectedStatus) {
        assertThat(scenarioContext.getResponse().getBody().get("status")).isEqualTo(expectedStatus);
    }

    @And("the join requests list is not empty")
    public void theJoinRequestsListIsNotEmpty() {
        assertThat(lastJoinRequestsList).isNotNull().isNotEmpty();
    }

    @And("the requester is now a member of the group")
    public void theRequesterIsNowAMemberOfTheGroup() {
        var response = restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId, HttpMethod.GET,
                authorizedEntity(null), Map.class);
        int memberCount = (int) response.getBody().get("memberCount");
        assertThat(memberCount).isGreaterThan(1);
    }

    @When("I submit a join request for the group without authentication")
    public void iSubmitAJoinRequestForTheGroupWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests", HttpMethod.POST,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I list join requests for the group without authentication")
    public void iListJoinRequestsForTheGroupWithoutAuthentication() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests", HttpMethod.GET,
                unauthenticatedEntity(null), Map.class));
    }

    @When("I handle a join request without authentication")
    public void iHandleAJoinRequestWithoutAuthentication() {
        UUID placeholderRequestId = UUID.randomUUID();
        Map<String, String> body = Map.of("action", "APPROVE");
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests/" + placeholderRequestId,
                HttpMethod.PUT,
                unauthenticatedEntity(body), Map.class));
    }

    @And("a non-existent join request id")
    public void aNonExistentJoinRequestId() {
        lastJoinRequestId = UUID.randomUUID();
    }

    @When("I handle the join request with no action")
    public void iHandleTheJoinRequestWithNoAction() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests/" + lastJoinRequestId,
                HttpMethod.PUT,
                authorizedEntity(Map.of()), Map.class));
    }

    @And("I have denied the join request")
    public void iHaveDeniedTheJoinRequest() {
        Map<String, String> body = Map.of("action", "DENY");
        restTemplate.exchange(
                baseUrl() + "/groups/" + currentGroupId + "/join-requests/" + lastJoinRequestId, HttpMethod.PUT,
                authorizedEntity(body), Map.class);
    }

    @And("the join request resolver is set")
    public void theJoinRequestResolverIsSet() {
        assertThat(scenarioContext.getResponse().getBody().get("resolvedBy")).isNotNull();
        assertThat(scenarioContext.getResponse().getBody().get("resolvedAt")).isNotNull();
    }

    @And("the join requests list is empty")
    public void theJoinRequestsListIsEmpty() {
        assertThat(lastJoinRequestsList).isNotNull().isEmpty();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private <T> HttpEntity<T> authorizedEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + scenarioContext.getAuthToken());
        return new HttpEntity<>(body, headers);
    }

    private <T> HttpEntity<T> unauthenticatedEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private <T> HttpEntity<T> entityWithToken(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(body, headers);
    }

    private void extractGroupId() {
        Map body = scenarioContext.getResponse().getBody();
        if (body != null && body.containsKey("id")) {
            currentGroupId = UUID.fromString((String) body.get("id"));
        }
    }

    private UUID getAuthenticatedUserId() {
        String token = scenarioContext.getAuthToken();
        String payload = token.split("\\.")[1];
        String decoded = new String(java.util.Base64.getUrlDecoder().decode(payload));
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> claims = mapper.readValue(decoded, Map.class);
            return UUID.fromString((String) claims.get("sub"));
        } catch (Exception e) {
            throw new IllegalStateException("Could not decode JWT token", e);
        }
    }

    private String registerAndLoginOtherUser() {
        String username = "other_user_" + System.nanoTime();
        String email = username + "@example.com";
        String password = "Str0ngP@ss!";
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(baseUrl() + "/auth/register",
                new HttpEntity<>(registerRequest, headers), Map.class);
        LoginRequest loginRequest = new LoginRequest(email, password);
        var loginResponse = restTemplate.postForEntity(baseUrl() + "/auth/login",
                new HttpEntity<>(loginRequest, headers), Map.class);
        return (String) loginResponse.getBody().get("token");
    }
}
