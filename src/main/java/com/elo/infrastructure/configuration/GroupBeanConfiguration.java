package com.elo.infrastructure.configuration;

import com.elo.application.group.port.in.CreateGroupPort;
import com.elo.application.group.port.in.GetGroupPort;
import com.elo.application.group.port.in.ListGroupsPort;
import com.elo.application.group.port.in.UpdateGroupPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.application.group.usecase.CreateGroupUseCase;
import com.elo.application.group.usecase.GetGroupUseCase;
import com.elo.application.group.usecase.ListGroupsUseCase;
import com.elo.application.group.usecase.UpdateGroupUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupBeanConfiguration {

    @Bean
    public CreateGroupPort createGroupPort(GroupRepositoryPort groupRepositoryPort,
                                           GroupMemberRepositoryPort groupMemberRepositoryPort) {
        return new CreateGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Bean
    public GetGroupPort getGroupPort(GroupRepositoryPort groupRepositoryPort,
                                     GroupMemberRepositoryPort groupMemberRepositoryPort) {
        return new GetGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Bean
    public UpdateGroupPort updateGroupPort(GroupRepositoryPort groupRepositoryPort,
                                           GroupMemberRepositoryPort groupMemberRepositoryPort) {
        return new UpdateGroupUseCase(groupRepositoryPort, groupMemberRepositoryPort);
    }

    @Bean
    public ListGroupsPort listGroupsPort(GroupRepositoryPort groupRepositoryPort) {
        return new ListGroupsUseCase(groupRepositoryPort);
    }
}
