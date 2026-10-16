package org.example.kitchenservice;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaConsumerConfig {
    private static final long RETRY_INTERVAL_MS = 2_000L;
    private static final long MAX_RETRIES = 2;
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + "-dlt", -1));
        FixedBackOff backOff = new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(
                JacksonException.class,
                NullPointerException.class,
                IllegalArgumentException.class
        );
        return errorHandler;
    }
}
