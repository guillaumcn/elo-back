package com.elo.domain.group.model;

import com.elo.domain.group.exception.InvalidGroupException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupTest {

    private final UUID creatorId = UUID.randomUUID();

    @Test
    void shouldCreateGroupWithValidData() {
        Group group = Group.create("Ping Pong Club", "A fun club", JoinPolicy.OPEN, creatorId);

        assertThat(group.getId()).isNotNull();
        assertThat(group.getName()).isEqualTo("Ping Pong Club");
        assertThat(group.getDescription()).isEqualTo("A fun club");
        assertThat(group.getJoinPolicy()).isEqualTo(JoinPolicy.OPEN);
        assertThat(group.isArchived()).isFalse();
        assertThat(group.getCreatedBy()).isEqualTo(creatorId);
        assertThat(group.getCreatedAt()).isNotNull();
        assertThat(group.getUpdatedAt()).isNotNull();
        assertThat(group.getMemberCount()).isZero();
    }

    @Test
    void shouldCreateGroupWithNullDescription() {
        Group group = Group.create("Chess Club", null, JoinPolicy.REQUEST, creatorId);

        assertThat(group.getDescription()).isNull();
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> Group.create("", "desc", JoinPolicy.OPEN, creatorId))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("Group name is required");
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThatThrownBy(() -> Group.create(null, "desc", JoinPolicy.OPEN, creatorId))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("Group name is required");
    }

    @Test
    void shouldThrowWhenNameExceedsMaxLength() {
        String tooLong = "a".repeat(101);
        assertThatThrownBy(() -> Group.create(tooLong, null, JoinPolicy.OPEN, creatorId))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("100 characters");
    }

    @Test
    void shouldThrowWhenJoinPolicyIsNull() {
        assertThatThrownBy(() -> Group.create("My Group", null, null, creatorId))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("Join policy is required");
    }

    @Test
    void shouldThrowWhenCreatorIsNull() {
        assertThatThrownBy(() -> Group.create("My Group", null, JoinPolicy.OPEN, null))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("Creator is required");
    }

    @Test
    void shouldUpdateGroupName() {
        Group group = Group.create("Old Name", null, JoinPolicy.OPEN, creatorId);
        group.update("New Name", null, null);

        assertThat(group.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldUpdateGroupDescription() {
        Group group = Group.create("My Group", "Old desc", JoinPolicy.OPEN, creatorId);
        group.update(null, "New desc", null);

        assertThat(group.getDescription()).isEqualTo("New desc");
        assertThat(group.getName()).isEqualTo("My Group");
    }

    @Test
    void shouldUpdateGroupJoinPolicy() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        group.update(null, null, JoinPolicy.INVITATION);

        assertThat(group.getJoinPolicy()).isEqualTo(JoinPolicy.INVITATION);
    }

    @Test
    void shouldUpdateUpdatedAtOnUpdate() throws InterruptedException {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        var originalUpdatedAt = group.getUpdatedAt();
        Thread.sleep(1);
        group.update("New Name", null, null);

        assertThat(group.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void shouldThrowWhenUpdatingWithBlankName() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);

        assertThatThrownBy(() -> group.update("", null, null))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("Group name is required");
    }

    @Test
    void shouldUpdateGroupDescriptionWhenWithinLimit() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        String validDescription = "a".repeat(1000);
        group.update(null, validDescription, null);

        assertThat(group.getDescription()).isEqualTo(validDescription);
    }

    @Test
    void shouldThrowWhenDescriptionExceeds1000Characters() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        String tooLong = "a".repeat(1001);

        assertThatThrownBy(() -> group.update(null, tooLong, null))
                .isInstanceOf(InvalidGroupException.class)
                .hasMessageContaining("1000 characters");
    }
}
