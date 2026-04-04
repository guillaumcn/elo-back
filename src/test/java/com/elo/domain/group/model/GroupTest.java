package com.elo.domain.group.model;

import com.elo.domain.group.exception.GroupAlreadyArchivedException;
import com.elo.domain.group.exception.GroupAlreadyMemberException;
import com.elo.domain.group.exception.GroupNotArchivedException;
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
        assertThat(group.getMemberCount()).isEqualTo(1);
        assertThat(group.getMembers()).hasSize(1);
        assertThat(group.getMembers().get(0).getUserId()).isEqualTo(creatorId);
        assertThat(group.getMembers().get(0).getRole()).isEqualTo(MemberRole.ADMIN);
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

    @Test
    void shouldArchiveGroup() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        group.archive();

        assertThat(group.isArchived()).isTrue();
    }

    @Test
    void shouldUpdateUpdatedAtOnArchive() throws InterruptedException {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        var originalUpdatedAt = group.getUpdatedAt();
        Thread.sleep(1);
        group.archive();

        assertThat(group.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void shouldThrowWhenArchivingAlreadyArchivedGroup() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        group.archive();

        assertThatThrownBy(group::archive)
                .isInstanceOf(GroupAlreadyArchivedException.class);
    }

    @Test
    void shouldUnarchiveGroup() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        group.archive();
        group.unarchive();

        assertThat(group.isArchived()).isFalse();
    }

    @Test
    void shouldUpdateUpdatedAtOnUnarchive() throws InterruptedException {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        group.archive();
        var archivedAt = group.getUpdatedAt();
        Thread.sleep(1);
        group.unarchive();

        assertThat(group.getUpdatedAt()).isAfter(archivedAt);
    }

    @Test
    void shouldThrowWhenUnarchivingNonArchivedGroup() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);

        assertThatThrownBy(group::unarchive)
                .isInstanceOf(GroupNotArchivedException.class);
    }

    @Test
    void shouldAddMemberToGroup() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        UUID newUserId = UUID.randomUUID();
        GroupMember newMember = GroupMember.createMember(group.getId(), newUserId);

        group.addMember(newMember);

        assertThat(group.getMemberCount()).isEqualTo(2);
        assertThat(group.hasMember(newUserId)).isTrue();
    }

    @Test
    void shouldThrowWhenAddingDuplicateMember() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        GroupMember duplicate = GroupMember.createMember(group.getId(), creatorId);

        assertThatThrownBy(() -> group.addMember(duplicate))
                .isInstanceOf(GroupAlreadyMemberException.class);
    }

    @Test
    void shouldDetectMemberPresence() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);

        assertThat(group.hasMember(creatorId)).isTrue();
        assertThat(group.hasMember(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldDetectAdminPresence() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);
        UUID memberId = UUID.randomUUID();
        group.addMember(GroupMember.createMember(group.getId(), memberId));

        assertThat(group.hasAdmin(creatorId)).isTrue();
        assertThat(group.hasAdmin(memberId)).isFalse();
    }

    @Test
    void shouldReturnUnmodifiableMembersList() {
        Group group = Group.create("My Group", null, JoinPolicy.OPEN, creatorId);

        assertThatThrownBy(() -> group.getMembers().add(GroupMember.createMember(group.getId(), UUID.randomUUID())))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
