package it.francescozoni.finanzepersonali.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationRunnerConfig {

    @Bean
    CommandLineRunner applicationRunner(
            MainController controller
    ) {
        return args -> controller.start();
    }
}
