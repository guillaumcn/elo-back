package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupCommand;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.JoinPolicy;
import com.elo.domain.group.model.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGroupUseCaseTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    private CreateGroupUseCase createGroupUseCase;

    private final UUID creatorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createGroupUseCase = new CreateGroupUseCase(groupRepositoryPort);
    }

    @Test
    void shouldCreateGroupAndReturnIt() {
        var command = new CreateGroupCommand(creatorId, "Ping Pong Club", "A fun club", JoinPolicy.OPEN);
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        Group result = createGroupUseCase.execute(command);

        assertThat(result.getName()).isEqualTo("Ping Pong Club");
        assertThat(result.getJoinPolicy()).isEqualTo(JoinPolicy.OPEN);
        verify(groupRepositoryPort).save(any(Group.class));
    }

    @Test
    void shouldIncludeCreatorAsAdminMember() {
        var command = new CreateGroupCommand(creatorId, "Chess Club", null, JoinPolicy.REQUEST);
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        Group result = createGroupUseCase.execute(command);

        assertThat(result.getMemberCount()).isEqualTo(1);
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getUserId()).isEqualTo(creatorId);
        assertThat(result.getMembers().get(0).getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void shouldSaveGroupWithMembersAtomically() {
        var command = new CreateGroupCommand(creatorId, "My Group", null, JoinPolicy.OPEN);
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        when(groupRepositoryPort.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        createGroupUseCase.execute(command);

        verify(groupRepositoryPort).save(groupCaptor.capture());
        Group savedGroup = groupCaptor.getValue();
        assertThat(savedGroup.getMembers()).hasSize(1);
        assertThat(savedGroup.getMembers().get(0).getRole()).isEqualTo(MemberRole.ADMIN);
    }
}
