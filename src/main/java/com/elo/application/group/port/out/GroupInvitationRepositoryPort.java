package com.elo.application.group.port.out;

import com.elo.domain.group.model.GroupInvitation;

import java.util.Optional;

public interface GroupInvitationRepositoryPort {

    GroupInvitation save(GroupInvitation invitation);

    Optional<GroupInvitation> findByToken(String token);
}
