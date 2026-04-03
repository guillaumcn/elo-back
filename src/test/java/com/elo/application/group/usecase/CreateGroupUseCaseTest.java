package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupCommand;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupMember;
import com.elo.domain.group.model.JoinPolicy;
import com.elo.domain.group.model.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private GroupMemberRepositoryPort groupMemberRepositoryPort;

    private CreateGroupUseCase createGroupUseCase;

    private final UUID creatorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createGroupUseCase = new CreateGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Test
    void shouldCreateGroupAndReturnIt() {
        var command = new CreateGroupCommand(creatorId, "Ping Pong Club", "A fun club", JoinPolicy.OPEN);
        Group savedGroup = buildGroup(command.name(), command.joinPolicy());
        when(groupRepositoryPort.save(any(Group.class))).thenReturn(savedGroup);
        when(groupMemberRepositoryPort.save(any(GroupMember.class))).thenAnswer(i -> i.getArgument(0));
        when(groupRepositoryPort.findById(savedGroup.getId())).thenReturn(Optional.of(savedGroup));

        Group result = createGroupUseCase.execute(command);

        assertThat(result.getName()).isEqualTo("Ping Pong Club");
        assertThat(result.getJoinPolicy()).isEqualTo(JoinPolicy.OPEN);
        verify(groupRepositoryPort).save(any(Group.class));
    }

    @Test
    void shouldAddCreatorAsAdminMember() {
        var command = new CreateGroupCommand(creatorId, "Chess Club", null, JoinPolicy.REQUEST);
        Group savedGroup = buildGroup(command.name(), command.joinPolicy());
        when(groupRepositoryPort.save(any(Group.class))).thenReturn(savedGroup);
        when(groupMemberRepositoryPort.save(any(GroupMember.class))).thenAnswer(i -> i.getArgument(0));
        when(groupRepositoryPort.findById(savedGroup.getId())).thenReturn(Optional.of(savedGroup));

        createGroupUseCase.execute(command);

        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepositoryPort).save(memberCaptor.capture());
        GroupMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUserId()).isEqualTo(creatorId);
        assertThat(savedMember.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void shouldReloadGroupAfterMemberAdded() {
        var command = new CreateGroupCommand(creatorId, "My Group", null, JoinPolicy.OPEN);
        Group savedGroup = buildGroup(command.name(), command.joinPolicy());
        Group reloadedGroup = Group.builder()
                .id(savedGroup.getId())
                .name(savedGroup.getName())
                .joinPolicy(savedGroup.getJoinPolicy())
                .archived(false)
                .createdBy(creatorId)
                .createdAt(savedGroup.getCreatedAt())
                .updatedAt(savedGroup.getUpdatedAt())
                .memberCount(1)
                .build();
        when(groupRepositoryPort.save(any(Group.class))).thenReturn(savedGroup);
        when(groupMemberRepositoryPort.save(any(GroupMember.class))).thenAnswer(i -> i.getArgument(0));
        when(groupRepositoryPort.findById(savedGroup.getId())).thenReturn(Optional.of(reloadedGroup));

        Group result = createGroupUseCase.execute(command);

        assertThat(result.getMemberCount()).isEqualTo(1);
    }

    private Group buildGroup(String name, JoinPolicy joinPolicy) {
        return Group.builder()
                .id(UUID.randomUUID())
                .name(name)
                .joinPolicy(joinPolicy)
                .archived(false)
                .createdBy(creatorId)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .memberCount(0)
                .build();
    }
}
