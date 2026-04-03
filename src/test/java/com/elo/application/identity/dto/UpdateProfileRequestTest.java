package com.elo.application.identity.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProfileRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidationWithAllNullFields() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWithValidHttpsAvatarUrl() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "https://example.com/avatar.png", null);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWithHttpAvatarUrl() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "http://example.com/avatar.png", null);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getMessage()))
                .anyMatch(msg -> msg.contains("HTTPS"));
    }

    @Test
    void shouldFailValidationWithAvatarUrlExceeding500Characters() {
        String longUrl = "https://" + "a".repeat(494);
        UpdateProfileRequest request = new UpdateProfileRequest(null, longUrl, null);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldPassValidationWithBioAtBoundary() {
        String bioAt500 = "a".repeat(500);
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, bioAt500);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWithBioExceeding500Characters() {
        String tooLongBio = "a".repeat(501);
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, tooLongBio);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getMessage()))
                .anyMatch(msg -> msg.contains("500 characters"));
    }

    @Test
    void shouldFailValidationWithUsernameShorterThan3Characters() {
        UpdateProfileRequest request = new UpdateProfileRequest("ab", null, null);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
