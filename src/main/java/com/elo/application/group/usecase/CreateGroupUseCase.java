package com.elo.application.group.usecase;

import com.elo.application.group.command.CreateGroupCommand;
import com.elo.application.group.port.in.CreateGroupPort;
import com.elo.application.group.port.out.GroupMemberRepositoryPort;
import com.elo.application.group.port.out.GroupRepositoryPort;
import com.elo.domain.group.model.Group;
import com.elo.domain.group.model.GroupMember;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateGroupUseCase implements CreateGroupPort {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupMemberRepositoryPort groupMemberRepositoryPort;

    @Override
    public Group execute(CreateGroupCommand command) {
        Group group = Group.create(command.name(), command.description(), command.joinPolicy(), command.creatorId());
        Group savedGroup = groupRepositoryPort.save(group);

        GroupMember adminMember = GroupMember.createAdmin(savedGroup.getId(), command.creatorId());
        groupMemberRepositoryPort.save(adminMember);

        return groupRepositoryPort.findById(savedGroup.getId()).orElseThrow();
    }
}
