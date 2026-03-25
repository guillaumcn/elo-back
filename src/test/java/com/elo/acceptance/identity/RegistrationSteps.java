package com.elo.acceptance.identity;

import com.elo.acceptance.ScenarioContext;
import com.elo.application.identity.dto.RegisterRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    private String pendingUsername;
    private String pendingEmail;
    private String pendingPassword;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Given("no user exists with email {string} or username {string}")
    public void noUserExists(String email, String username) {
        // Fresh database per test run — no action needed
    }

    @Given("a registration request with username {string}")
    public void aRegistrationRequestWithUsername(String username) {
        pendingUsername = username;
        pendingEmail = null;
        pendingPassword = null;
    }

    @And("the registration email is {string}")
    public void theRegistrationEmailIs(String email) {
        pendingEmail = email;
    }

    @And("the registration password is {string}")
    public void theRegistrationPasswordIs(String password) {
        pendingPassword = password;
    }

    @When("I submit the registration request")
    public void iSubmitTheRegistrationRequest() {
        RegisterRequest request = new RegisterRequest(pendingUsername, pendingEmail, pendingPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        scenarioContext.setResponse(restTemplate.postForEntity(
                baseUrl() + "/auth/register",
                new HttpEntity<>(request, headers),
                Map.class));
    }

    @And("the response contains the user profile with username {string}")
    public void responseContainsUserProfile(String username) {
        Map body = scenarioContext.getResponse().getBody();
        assertThat(body).isNotNull();
        Map user = (Map) body.get("user");
        assertThat(user).isNotNull();
        assertThat(user.get("username")).isEqualTo(username);
        assertThat(user.get("id")).isNotNull();
        assertThat(user.get("email")).isNotNull();
        assertThat(user.get("createdAt")).isNotNull();
    }

    @And("the error message indicates the username is already taken")
    public void errorMessageIndicatesUsernameTaken() {
        Map body = scenarioContext.getResponse().getBody();
        assertThat(body).isNotNull();
        assertThat((String) body.get("message")).containsIgnoringCase("username");
    }

    @And("the error message indicates the email is already taken")
    public void errorMessageIndicatesEmailTaken() {
        Map body = scenarioContext.getResponse().getBody();
        assertThat(body).isNotNull();
        assertThat((String) body.get("message")).containsIgnoringCase("email");
    }
}
