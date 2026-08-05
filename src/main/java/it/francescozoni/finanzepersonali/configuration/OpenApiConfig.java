package it.francescozoni.finanzepersonali.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI finanzeOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("Finanze Personali API")
                        .description("API per la gestione delle finanze personali")
                        .version("1.0.0")
        );
    }
}
