package com.elo.acceptance;

import com.elo.application.identity.dto.RegisterRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    // ── Shared setup steps ──────────────────────────────────────────────────

    @Given("a user exists with username {string}")
    public void aUserExistsWithUsername(String username) {
        register(username, username + "@test.com", "Str0ngP@ss!");
    }

    @Given("a user exists with email {string}")
    public void aUserExistsWithEmail(String email) {
        register("existinguser_" + System.nanoTime(), email, "Str0ngP@ss!");
    }

    // ── Generic status assertion steps ──────────────────────────────────────

    @Then("I receive a 200 OK response")
    public void iReceive200OkResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(200);
    }

    @Then("I receive a 201 Created response")
    public void iReceive201CreatedResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(201);
    }

    @Then("I receive a 204 No Content response")
    public void iReceive204NoContentResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(204);
    }

    @Then("I receive a 400 Bad Request response")
    public void iReceive400BadRequestResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(400);
    }

    @Then("I receive a 401 Unauthorized response")
    public void iReceive401UnauthorizedResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(401);
    }

    @Then("I receive a 404 Not Found response")
    public void iReceive404NotFoundResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(404);
    }

    @Then("I receive a 409 Conflict response")
    public void iReceive409ConflictResponse() {
        assertThat(scenarioContext.getResponse().getStatusCode().value()).isEqualTo(409);
    }

    // ── Shared response assertion steps ─────────────────────────────────────

    @And("the response contains a valid JWT token")
    public void responseContainsJwtToken() {
        Map body = scenarioContext.getResponse().getBody();
        assertThat(body).isNotNull();
        String token = (String) body.get("token");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(baseUrl() + "/auth/register",
                new HttpEntity<>(request, headers), Map.class);
    }
}
