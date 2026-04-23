package com.elo.infrastructure.configuration;

import com.elo.application.group.port.in.ArchiveGroupPort;
import com.elo.application.group.port.in.CreateGroupInvitationPort;
import com.elo.application.group.port.in.CreateGroupPort;
import com.elo.application.group.port.in.GetGroupPort;
import com.elo.application.group.port.in.HandleJoinRequestPort;
import com.elo.application.group.port.in.JoinGroupByInvitationPort;
import com.elo.application.group.port.in.JoinGroupPort;
import com.elo.application.group.port.in.ListGroupsPort;
import com.elo.application.group.port.in.ListJoinRequestsPort;
import com.elo.application.group.port.in.SubmitJoinRequestPort;
import com.elo.application.group.port.in.UnarchiveGroupPort;
import com.elo.application.group.port.in.UpdateGroupPort;
import com.elo.application.group.port.out.GroupInvitationRepositoryPort;
import com.elo.application.group.port.out.GroupJoinRequestRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.group.usecase.ArchiveGroupUseCase;
import com.elo.application.group.usecase.CreateGroupInvitationUseCase;
import com.elo.application.group.usecase.CreateGroupUseCase;
import com.elo.application.group.usecase.GetGroupUseCase;
import com.elo.application.group.usecase.HandleJoinRequestUseCase;
import com.elo.application.group.usecase.JoinGroupByInvitationUseCase;
import com.elo.application.group.usecase.JoinGroupUseCase;
import com.elo.application.group.usecase.ListGroupsUseCase;
import com.elo.application.group.usecase.ListJoinRequestsUseCase;
import com.elo.application.group.usecase.SubmitJoinRequestUseCase;
import com.elo.application.group.usecase.UnarchiveGroupUseCase;
import com.elo.application.group.usecase.UpdateGroupUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupBeanConfiguration {

    @Bean
    public CreateGroupPort createGroupPort(GroupRepositoryPort groupRepositoryPort) {
        return new CreateGroupUseCase(groupRepositoryPort);
    }

    @Bean
    public GetGroupPort getGroupPort(GroupRepositoryPort groupRepositoryPort) {
        return new GetGroupUseCase(groupRepositoryPort);
    }

    @Bean
    public UpdateGroupPort updateGroupPort(GroupRepositoryPort groupRepositoryPort) {
        return new UpdateGroupUseCase(groupRepositoryPort);
    }

    @Bean
    public ListGroupsPort listGroupsPort(GroupRepositoryPort groupRepositoryPort) {
        return new ListGroupsUseCase(groupRepositoryPort);
    }

    @Bean
    public ArchiveGroupPort archiveGroupPort(GroupRepositoryPort groupRepositoryPort) {
        return new ArchiveGroupUseCase(groupRepositoryPort);
    }

    @Bean
    public UnarchiveGroupPort unarchiveGroupPort(GroupRepositoryPort groupRepositoryPort) {
        return new UnarchiveGroupUseCase(groupRepositoryPort);
    }

    @Bean
    public JoinGroupPort joinGroupPort(GroupRepositoryPort groupRepositoryPort) {
        return new JoinGroupUseCase(groupRepositoryPort);
    }

    @Bean
    public CreateGroupInvitationPort createGroupInvitationPort(GroupRepositoryPort groupRepositoryPort,
                                                                GroupInvitationRepositoryPort groupInvitationRepositoryPort) {
        return new CreateGroupInvitationUseCase(groupRepositoryPort, groupInvitationRepositoryPort);
    }

    @Bean
    public JoinGroupByInvitationPort joinGroupByInvitationPort(GroupRepositoryPort groupRepositoryPort,
                                                                GroupInvitationRepositoryPort groupInvitationRepositoryPort) {
        return new JoinGroupByInvitationUseCase(groupRepositoryPort, groupInvitationRepositoryPort);
    }

    @Bean
    public SubmitJoinRequestPort submitJoinRequestPort(GroupRepositoryPort groupRepositoryPort,
                                                       GroupJoinRequestRepositoryPort joinRequestRepositoryPort) {
        return new SubmitJoinRequestUseCase(groupRepositoryPort, joinRequestRepositoryPort);
    }

    @Bean
    public ListJoinRequestsPort listJoinRequestsPort(GroupRepositoryPort groupRepositoryPort,
                                                     GroupJoinRequestRepositoryPort joinRequestRepositoryPort) {
        return new ListJoinRequestsUseCase(groupRepositoryPort, joinRequestRepositoryPort);
    }

    @Bean
    public HandleJoinRequestPort handleJoinRequestPort(GroupRepositoryPort groupRepositoryPort,
                                                       GroupJoinRequestRepositoryPort joinRequestRepositoryPort) {
        return new HandleJoinRequestUseCase(groupRepositoryPort, joinRequestRepositoryPort);
    }
}
