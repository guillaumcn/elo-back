package com.elo.infrastructure.configuration;

import com.elo.application.identity.port.in.RegisterUserPort;
import com.elo.application.identity.usecase.RegisterUserUseCase;
import com.elo.application.identity.port.out.PasswordHasherPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityBeanConfiguration {

    @Bean
    public RegisterUserPort registerUserPort(UserRepositoryPort userRepositoryPort,
                                             PasswordHasherPort passwordHasher) {
        return new RegisterUserUseCase(userRepositoryPort, passwordHasher);
    }
}
