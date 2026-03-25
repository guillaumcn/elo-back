package com.elo.acceptance.identity;

import com.elo.application.identity.dto.RegisterRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<Map> response;

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

    @Given("a user exists with username {string}")
    public void aUserExistsWithUsername(String username) {
        register(username, username + "@test.com", "Str0ngP@ss!");
    }

    @Given("a user exists with email {string}")
    public void aUserExistsWithEmail(String email) {
        register("existinguser_" + System.nanoTime(), email, "Str0ngP@ss!");
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
        response = register(pendingUsername, pendingEmail, pendingPassword);
    }

    @Then("I receive a {int} Created response")
    public void iReceiveCreatedResponse(int statusCode) {
        assertThat(response.getStatusCode().value()).isEqualTo(statusCode);
    }

    @Then("I receive a {int} Conflict response")
    public void iReceiveConflictResponse(int statusCode) {
        assertThat(response.getStatusCode().value()).isEqualTo(statusCode);
    }

    @Then("I receive a {int} Bad Request response")
    public void iReceiveBadRequestResponse(int statusCode) {
        assertThat(response.getStatusCode().value()).isEqualTo(statusCode);
    }

    @And("the response contains a valid JWT token")
    public void responseContainsJwtToken() {
        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("token")).isNotNull();
        String token = (String) body.get("token");
        assertThat(token).isNotBlank();
        // JWT has 3 parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @And("the response contains the user profile with username {string}")
    public void responseContainsUserProfile(String username) {
        Map body = response.getBody();
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
        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) body.get("message")).containsIgnoringCase("username");
    }

    @And("the error message indicates the email is already taken")
    public void errorMessageIndicatesEmailTaken() {
        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) body.get("message")).containsIgnoringCase("email");
    }

    private ResponseEntity<Map> register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForEntity(baseUrl() + "/auth/register", entity, Map.class);
    }
}
