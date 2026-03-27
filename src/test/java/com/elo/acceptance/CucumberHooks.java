package com.elo.acceptance;

import com.elo.infrastructure.adapter.out.persistence.identity.UserJpaRepository;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CucumberHooks {

    private final UserJpaRepository userJpaRepository;

    @Before
    public void cleanDatabase() {
        userJpaRepository.deleteAll();
    }
}
