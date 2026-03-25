package com.elo.acceptance.identity;

import com.elo.application.identity.dto.LoginRequest;
import com.elo.application.identity.dto.RegisterRequest;
import com.elo.infrastructure.adapter.out.persistence.identity.UserJpaEntity;
import com.elo.infrastructure.adapter.out.persistence.identity.UserJpaRepository;
import com.elo.infrastructure.configuration.JwtProperties;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
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
import org.springframework.http.ResponseEntity;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private JwtProperties jwtProperties;

    private ResponseEntity<Map> response;

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
        userJpaRepository.findByEmail(email).ifPresent(existing ->
                userJpaRepository.save(UserJpaEntity.builder()
                        .id(existing.getId())
                        .username(existing.getUsername())
                        .email(existing.getEmail())
                        .passwordHash(existing.getPasswordHash())
                        .avatarUrl(existing.getAvatarUrl())
                        .bio(existing.getBio())
                        .deleted(true)
                        .createdAt(existing.getCreatedAt())
                        .updatedAt(Instant.now())
                        .build())
        );
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
        response = login(pendingEmail, pendingPassword);
    }

    @When("I access a protected endpoint without a token")
    public void iAccessProtectedEndpointWithoutToken() {
        response = restTemplate.getForEntity(baseUrl() + "/users/me", Map.class);
    }

    @When("I access a protected endpoint with an expired token")
    public void iAccessProtectedEndpointWithExpiredToken() {
        String expiredToken = buildExpiredToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + expiredToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(baseUrl() + "/users/me", HttpMethod.GET, entity, Map.class);
    }

    @Then("I receive a 200 OK response")
    public void iReceive200OkResponse() {
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Then("I receive a 401 Unauthorized response")
    public void iReceive401UnauthorizedResponse() {
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    private ResponseEntity<Map> login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForEntity(baseUrl() + "/auth/login", entity, Map.class);
    }

    private void register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.postForEntity(baseUrl() + "/auth/register", entity, Map.class);
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
