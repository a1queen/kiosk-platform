package org.example.kitchenservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    @DisplayName("Should configure OpenAPI metadata correctly")
    void shouldConfigureOpenApiMetadata() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.kitchenOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Kitchen Service API");
        assertThat(openAPI.getInfo().getDescription()).isEqualTo("REST API for order processing in kitchen");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1.0.0");
    }
}
