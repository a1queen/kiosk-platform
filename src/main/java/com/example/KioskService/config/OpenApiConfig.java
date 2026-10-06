package com.example.KioskService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kioskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kiosk Service API")
                        .description("REST API for order processing in self-service kiosks")
                        .version("v1.0.0"));
    }
}
