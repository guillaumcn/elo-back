package com.elo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class EloRankingApplication {

    private static final Logger log = LoggerFactory.getLogger(EloRankingApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EloRankingApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        log.info("Swagger UI: http://localhost:{}/swagger-ui/index.html", port);
    }
}
