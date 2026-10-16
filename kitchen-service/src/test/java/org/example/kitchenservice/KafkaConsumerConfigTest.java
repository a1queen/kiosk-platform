package org.example.kitchenservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaConsumerConfigTest {

    @Test
    @DisplayName("Should create and configure DefaultErrorHandler bean")
    void shouldCreateDefaultErrorHandlerWithProperConfiguration() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        KafkaTemplate<?, ?> kafkaTemplate = mock(KafkaTemplate.class);

        DefaultErrorHandler errorHandler = config.errorHandler(kafkaTemplate);

        assertThat(errorHandler).isNotNull();
    }
}
