package com.elo.acceptance.identity;

import com.elo.acceptance.ScenarioContext;
import com.elo.application.identity.dto.LoginRequest;
import com.elo.application.identity.dto.RegisterRequest;
import com.elo.application.identity.dto.UpdateProfileRequest;
import com.elo.infrastructure.adapter.out.persistence.identity.UserJpaRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private String pendingUsername;
    private String pendingAvatarUrl;
    private String pendingBio;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String username) {
        String email = username + "@example.com";
        register(username, email, "Str0ngP@ss!");
        scenarioContext.setAuthToken(login(email, "Str0ngP@ss!"));
    }

    @And("a profile update request with username {string}")
    public void aProfileUpdateRequestWithUsername(String username) {
        pendingUsername = username;
        pendingAvatarUrl = null;
        pendingBio = null;
    }

    @And("a profile update request with bio {string}")
    public void aProfileUpdateRequestWithBio(String bio) {
        pendingUsername = null;
        pendingAvatarUrl = null;
        pendingBio = bio;
    }

    @And("a profile update request with avatar URL {string}")
    public void aProfileUpdateRequestWithAvatarUrl(String avatarUrl) {
        pendingUsername = null;
        pendingAvatarUrl = avatarUrl;
        pendingBio = null;
    }

    @And("the profile update bio is {string}")
    public void theProfileUpdateBioIs(String bio) {
        pendingBio = bio;
    }

    @When("I request my profile")
    public void iRequestMyProfile() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/users/me", HttpMethod.GET,
                authorizedEntity(null), Map.class));
    }

    @When("I submit the profile update")
    public void iSubmitTheProfileUpdate() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/users/me", HttpMethod.PUT,
                authorizedEntity(new UpdateProfileRequest(pendingUsername, pendingAvatarUrl, pendingBio)),
                Map.class));
    }

    @When("I delete my account")
    public void iDeleteMyAccount() {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/users/me", HttpMethod.DELETE,
                authorizedEntity(null), Map.class));
    }

    @When("I request the public profile of {string}")
    public void iRequestThePublicProfileOf(String username) {
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/users/" + username, HttpMethod.GET,
                authorizedEntity(null), Map.class));
    }

    @And("the profile username is {string}")
    public void theProfileUsernameIs(String expectedUsername) {
        assertThat(scenarioContext.getResponse().getBody().get("username")).isEqualTo(expectedUsername);
    }

    @And("the public profile username is {string}")
    public void thePublicProfileUsernameIs(String expectedUsername) {
        assertThat(scenarioContext.getResponse().getBody().get("username")).isEqualTo(expectedUsername);
    }

    @And("the profile bio is {string}")
    public void theProfileBioIs(String expectedBio) {
        assertThat(scenarioContext.getResponse().getBody().get("bio")).isEqualTo(expectedBio);
    }

    @And("the public profile does not contain an email")
    public void thePublicProfileDoesNotContainAnEmail() {
        assertThat(scenarioContext.getResponse().getBody()).doesNotContainKey("email");
    }

    @And("I can no longer authenticate as {string}")
    public void iCanNoLongerAuthenticateAs(String username) {
        ResponseEntity<Map> loginResponse = loginForResponse(username + "@example.com", "Str0ngP@ss!");
        assertThat(loginResponse.getStatusCode().value()).isEqualTo(401);
    }

    @And("the account data is anonymized")
    public void theAccountDataIsAnonymized() {
        userJpaRepository.findAll().stream()
                .filter(u -> u.isDeleted())
                .forEach(u -> {
                    assertThat(u.getUsername()).startsWith("deleted_");
                    assertThat(u.getAvatarUrl()).isNull();
                    assertThat(u.getBio()).isNull();
                });
    }

    private <T> HttpEntity<T> authorizedEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + scenarioContext.getAuthToken());
        return new HttpEntity<>(body, headers);
    }

    private void register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(baseUrl() + "/auth/register",
                new HttpEntity<>(request, headers), Map.class);
    }

    private String login(String email, String password) {
        return (String) loginForResponse(email, password).getBody().get("token");
    }

    private ResponseEntity<Map> loginForResponse(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(baseUrl() + "/auth/login",
                new HttpEntity<>(request, headers), Map.class);
    }
}
