package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupCommand;
import com.elo.application.group.port.in.CreateGroupPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateGroupUseCase implements CreateGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public Group execute(CreateGroupCommand command) {
        Group group = Group.create(command.name(), command.description(), command.joinPolicy(), command.creatorId());
        return groupRepositoryPort.save(group);
    }
}
