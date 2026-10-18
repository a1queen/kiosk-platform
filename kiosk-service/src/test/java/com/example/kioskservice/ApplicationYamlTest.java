package com.example.kioskservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationYamlTest {

    @Test
    @DisplayName("Should successfully load and validate application.yml configuration properties")
    void shouldLoadAndValidateApplicationYaml() {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties properties = yamlFactory.getObject();

        assertThat(properties).isNotNull();

        // Spring application name
        assertThat(properties.getProperty("spring.application.name")).isEqualTo("kiosk-service");

        // Kafka properties
        assertThat(properties.getProperty("spring.kafka.bootstrap-servers"))
                .isEqualTo("${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}");
        assertThat(properties.getProperty("spring.kafka.producer.key-serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringSerializer");
        assertThat(properties.getProperty("spring.kafka.producer.value-serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringSerializer");
        assertThat(properties.getProperty("spring.kafka.consumer.group-id")).isEqualTo("kiosk-order-status-group");
        assertThat(properties.getProperty("spring.kafka.consumer.auto-offset-reset")).isEqualTo("earliest");
        assertThat(properties.getProperty("spring.kafka.consumer.key-deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringDeserializer");
        assertThat(properties.getProperty("spring.kafka.consumer.value-deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringDeserializer");
        assertThat(properties.getProperty("spring.kafka.listener.ack-mode")).isEqualTo("record");

        // Datasource properties
        assertThat(properties.getProperty("spring.datasource.url"))
                .isEqualTo("${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/kiosk_db}");
        assertThat(properties.getProperty("spring.datasource.username"))
                .isEqualTo("${SPRING_DATASOURCE_USERNAME:kiosk_user}");
        assertThat(properties.getProperty("spring.datasource.password"))
                .isEqualTo("${SPRING_DATASOURCE_PASSWORD:kiosk_password}");
        assertThat(properties.getProperty("spring.datasource.driver-class-name")).isEqualTo("org.postgresql.Driver");

        // JPA / Hibernate properties
        assertThat(properties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(properties.getProperty("spring.jpa.show-sql")).isEqualTo("true");
        assertThat(properties.getProperty("spring.jpa.properties.hibernate.format_sql")).isEqualTo("true");

        // Flyway properties
        assertThat(properties.getProperty("spring.flyway.enabled")).isEqualTo("true");
        assertThat(properties.getProperty("spring.flyway.baseline-on-migrate")).isEqualTo("true");
    }
}
