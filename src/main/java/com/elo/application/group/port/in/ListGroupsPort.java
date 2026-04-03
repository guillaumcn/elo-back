package com.elo.application.group.port.in;

import com.elo.domain.group.model.Group;

import java.util.List;
import java.util.UUID;

public interface ListGroupsPort {

    List<Group> execute(UUID userId);
}
