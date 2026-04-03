package com.elo.application.group.dto;

import com.elo.domain.group.model.JoinPolicy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateGroupRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        CreateGroupRequest request = new CreateGroupRequest("Ping Pong Club", "A fun club", JoinPolicy.OPEN);
        Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWithNullDescription() {
        CreateGroupRequest request = new CreateGroupRequest("Chess Club", null, JoinPolicy.REQUEST);
        Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWithDescriptionAtBoundary() {
        String descriptionAt1000 = "a".repeat(1000);
        CreateGroupRequest request = new CreateGroupRequest("My Group", descriptionAt1000, JoinPolicy.OPEN);
        Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWithDescriptionExceeding1000Characters() {
        String tooLong = "a".repeat(1001);
        CreateGroupRequest request = new CreateGroupRequest("My Group", tooLong, JoinPolicy.OPEN);
        Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getMessage()))
                .anyMatch(msg -> msg.contains("1000 characters"));
    }

    @Test
    void shouldFailValidationWithBlankName() {
        CreateGroupRequest request = new CreateGroupRequest("", "desc", JoinPolicy.OPEN);
        Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
