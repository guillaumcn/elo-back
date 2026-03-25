package com.elo.acceptance.identity;

import com.elo.acceptance.ScenarioContext;
import com.elo.application.identity.dto.LoginRequest;
import com.elo.application.identity.dto.RegisterRequest;
import com.elo.infrastructure.configuration.JwtProperties;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class LoginSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private JwtProperties jwtProperties;

    private String pendingEmail;
    private String pendingPassword;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Given("a registered user with email {string} and password {string}")
    public void aRegisteredUserWithEmailAndPassword(String email, String password) {
        String username = "user_" + email.replace("@", "_").replace(".", "_");
        register(username, email, password);
    }

    @Given("a user with email {string} has deleted their account")
    public void aUserWithEmailHasDeletedTheirAccount(String email) {
        String username = "deleted_" + System.nanoTime();
        register(username, email, "Str0ngP@ss!");
        String token = login(email, "Str0ngP@ss!");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        restTemplate.exchange(baseUrl() + "/users/me", HttpMethod.DELETE,
                new HttpEntity<>(headers), Map.class);
    }

    @Given("a login request with email {string}")
    public void aLoginRequestWithEmail(String email) {
        pendingEmail = email;
        pendingPassword = null;
    }

    @And("the login password is {string}")
    public void theLoginPasswordIs(String password) {
        pendingPassword = password;
    }

    @When("I submit the login request")
    public void iSubmitTheLoginRequest() {
        scenarioContext.setResponse(loginForResponse(pendingEmail, pendingPassword));
    }

    @When("I access a protected endpoint without a token")
    public void iAccessProtectedEndpointWithoutToken() {
        scenarioContext.setResponse(
                restTemplate.getForEntity(baseUrl() + "/users/me", Map.class));
    }

    @When("I access a protected endpoint with an expired token")
    public void iAccessProtectedEndpointWithExpiredToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + buildExpiredToken());
        scenarioContext.setResponse(restTemplate.exchange(
                baseUrl() + "/users/me", HttpMethod.GET,
                new HttpEntity<>(headers), Map.class));
    }

    private String login(String email, String password) {
        return (String) loginForResponse(email, password).getBody().get("token");
    }

    private org.springframework.http.ResponseEntity<Map> loginForResponse(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(baseUrl() + "/auth/login",
                new HttpEntity<>(request, headers), Map.class);
    }

    private void register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(baseUrl() + "/auth/register",
                new HttpEntity<>(request, headers), Map.class);
    }

    private String buildExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 3600000);
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date(past.getTime() - 60000))
                .expiration(past)
                .signWith(key)
                .compact();
    }
}
