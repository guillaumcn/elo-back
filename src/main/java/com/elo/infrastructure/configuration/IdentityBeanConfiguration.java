package com.elo.infrastructure.configuration;

import com.elo.application.identity.port.in.DeleteAccountPort;
import com.elo.application.identity.port.in.GetOwnProfilePort;
import com.elo.application.identity.port.in.GetPublicProfilePort;
import com.elo.application.identity.port.in.LoginUserPort;
import com.elo.application.identity.port.in.RegisterUserPort;
import com.elo.application.identity.port.in.UpdateProfilePort;
import com.elo.application.identity.port.out.PasswordHasherPort;
import com.elo.application.identity.port.out.UserRepositoryPort;
import com.elo.application.identity.port.out.UserVisibilityPort;
import com.elo.application.identity.usecase.DeleteAccountUseCase;
import com.elo.application.identity.usecase.GetOwnProfileUseCase;
import com.elo.application.identity.usecase.GetPublicProfileUseCase;
import com.elo.application.identity.usecase.LoginUserUseCase;
import com.elo.application.identity.usecase.RegisterUserUseCase;
import com.elo.application.identity.usecase.UpdateProfileUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityBeanConfiguration {

    @Bean
    public RegisterUserPort registerUserPort(UserRepositoryPort userRepositoryPort,
                                             PasswordHasherPort passwordHasher) {
        return new RegisterUserUseCase(userRepositoryPort, passwordHasher);
    }

    @Bean
    public LoginUserPort loginUserPort(UserRepositoryPort userRepositoryPort,
                                       PasswordHasherPort passwordHasher) {
        return new LoginUserUseCase(userRepositoryPort, passwordHasher);
    }

    @Bean
    public GetOwnProfilePort getOwnProfilePort(UserRepositoryPort userRepositoryPort) {
        return new GetOwnProfileUseCase(userRepositoryPort);
    }

    @Bean
    public UpdateProfilePort updateProfilePort(UserRepositoryPort userRepositoryPort) {
        return new UpdateProfileUseCase(userRepositoryPort);
    }

    @Bean
    public DeleteAccountPort deleteAccountPort(UserRepositoryPort userRepositoryPort) {
        return new DeleteAccountUseCase(userRepositoryPort);
    }

    @Bean
    public GetPublicProfilePort getPublicProfilePort(UserRepositoryPort userRepositoryPort,
                                                     UserVisibilityPort userVisibilityPort) {
        return new GetPublicProfileUseCase(userRepositoryPort, userVisibilityPort);
    }
}
