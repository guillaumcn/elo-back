package com.elo.application.group.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateGroupRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidationWithAllNullFields() {
        UpdateGroupRequest request = new UpdateGroupRequest(null, null, null);
        Set<ConstraintViolation<UpdateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWithDescriptionAtBoundary() {
        String descriptionAt1000 = "a".repeat(1000);
        UpdateGroupRequest request = new UpdateGroupRequest(null, descriptionAt1000, null);
        Set<ConstraintViolation<UpdateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWithDescriptionExceeding1000Characters() {
        String tooLong = "a".repeat(1001);
        UpdateGroupRequest request = new UpdateGroupRequest(null, tooLong, null);
        Set<ConstraintViolation<UpdateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getMessage()))
                .anyMatch(msg -> msg.contains("1000 characters"));
    }
}
