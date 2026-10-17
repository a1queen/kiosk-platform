package org.example.kitchenservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_TITLE = "Kitchen Service API";
    private static final String API_DESCRIPTION = "REST API for order processing in kitchen";
    private static final String API_VERSION = "v1.0.0";

    @Bean
    public OpenAPI kitchenOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_TITLE)
                        .description(API_DESCRIPTION)
                        .version(API_VERSION));
    }
}
