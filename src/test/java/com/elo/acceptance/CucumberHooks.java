package com.elo.acceptance;

import com.elo.infrastructure.adapter.out.persistence.group.GroupInvitationJpaRepository;
import com.elo.infrastructure.adapter.out.persistence.group.GroupMemberJpaRepository;
import com.elo.infrastructure.adapter.out.persistence.group.GroupJpaRepository;
import com.elo.infrastructure.adapter.out.persistence.identity.UserJpaRepository;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CucumberHooks {

    private final GroupInvitationJpaRepository groupInvitationJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;
    private final GroupJpaRepository groupJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Before
    public void cleanDatabase() {
        groupInvitationJpaRepository.deleteAll();
        groupMemberJpaRepository.deleteAll();
        groupJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }
}
